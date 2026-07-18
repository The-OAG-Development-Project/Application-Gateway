package org.owasp.oag.persistentmap;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * A {@link PersistentMap} backed by an append-only log file. Every {@code put}/{@code remove}
 * appends a single JSON record (one per line) and flushes it before returning, giving
 * durability across restarts without any Java serialization.
 * <p>
 * To keep the log from growing without bound the file is compacted (rewritten from the live
 * in-memory state, atomically) once enough records have been appended since the last
 * compaction. Compaction is an internal concern and is never exposed on the interface.
 *
 * @param <V> the value type
 */
public class FilePersistentMap<V> implements PersistentMap<V> {

    /** Multiplier applied to the live entry count when deciding whether to compact. */
    private static final int COMPACTION_FACTOR = 2;

    /** Minimum number of appends tolerated before compaction is considered. */
    private static final int MIN_APPENDS_BEFORE_COMPACTION = 1000;

    private static final Logger log = LoggerFactory.getLogger(FilePersistentMap.class);

    /** In-memory mirror of the persisted entries; reads are served from here. */
    private final ConcurrentHashMap<String, V> map = new ConcurrentHashMap<>();

    /** Absolute path of the log file. */
    private final Path file;

    /** Directory containing {@link #file}, used for temp files and fsync. */
    private final Path directory;

    /** Jackson mapper used to (de)serialize log records. */
    private final ObjectMapper objectMapper;

    /** Fully resolved type of {@code LogRecord<V>} for deserialization. */
    private final JavaType recordType;

    /** Guards all writes and the compaction writer swap. */
    private final ReentrantLock writeLock = new ReentrantLock();

    /** Append writer on {@link #file}; replaced during compaction. */
    private Writer writer;

    /** Number of records appended since the last compaction. */
    private int appendsSinceCompaction;

    /**
     * Creates a file-backed map, replaying any existing log file.
     *
     * @param filename  the log file path
     * @param valueType the runtime class of the value type, used for deserialization
     */
    public FilePersistentMap(String filename, Class<V> valueType) {
        this(filename, valueType, new ObjectMapper());
    }

    /**
     * Creates a file-backed map with a custom Jackson mapper.
     *
     * @param filename     the log file path
     * @param valueType    the runtime class of the value type, used for deserialization
     * @param objectMapper the Jackson mapper to use
     */
    FilePersistentMap(String filename, Class<V> valueType, ObjectMapper objectMapper) {
        this.file = Paths.get(filename).toAbsolutePath();
        this.directory = this.file.getParent();
        this.objectMapper = objectMapper;
        this.recordType = objectMapper.getTypeFactory().constructParametricType(LogRecord.class, valueType);
        load();
    }

    @Override
    public V get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, V value) {
        Objects.requireNonNull(value, "value must not be null");
        writeLock.lock();
        try {
            map.put(key, value);
            append(new LogRecord<>(key, value));
            maybeCompact();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(String key) {
        writeLock.lock();
        try {
            if (map.remove(key) != null) {
                append(new LogRecord<>(key, null));
                maybeCompact();
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            map.clear();
            compactLocked();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Set<Map.Entry<String, V>> entrySet() {
        return map.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void close() {
        writeLock.lock();
        try {
            closeWriter();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to close " + file, e);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Replays the existing log file into memory and opens the append writer. Records are
     * applied directly to the in-memory map (never through {@link #put}/{@link #remove}) so a
     * restart does not re-append existing entries. Unreadable content is skipped; if any was
     * seen the file is compacted once to rewrite it in the clean format.
     */
    private void load() {
        boolean sawUnreadable = false;
        if (Files.exists(file)) {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file), decoder))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    if (!apply(line)) {
                        sawUnreadable = true;
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read " + file, e);
            }
        }

        try {
            openWriter();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to open " + file, e);
        }

        if (sawUnreadable) {
            log.warn("Persistent map file {} contained unreadable content; rewriting in clean format", file);
            compactLocked();
        }
    }

    /**
     * Applies a single log line to the in-memory map.
     *
     * @param line the raw JSON line
     * @return {@code true} if the line was a valid record, {@code false} if it was unreadable
     */
    private boolean apply(String line) {
        LogRecord<V> record;
        try {
            record = objectMapper.readValue(line, recordType);
        } catch (IOException parseError) {
            return false;
        }
        if (record.k == null) {
            return false;
        }
        if (record.v == null) {
            map.remove(record.k);
        } else {
            map.put(record.k, record.v);
        }
        return true;
    }

    /**
     * Appends a record to the log and flushes it. Must be called while holding {@link #writeLock}.
     *
     * @param record the record to append
     */
    private void append(LogRecord<V> record) {
        try {
            writer.write(objectMapper.writeValueAsString(record));
            writer.write('\n');
            writer.flush();
            appendsSinceCompaction++;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to append to " + file, e);
        }
    }

    /**
     * Compacts the log when the number of appends since the last compaction exceeds a
     * threshold proportional to the live entry count.
     */
    private void maybeCompact() {
        if (appendsSinceCompaction > (long) map.size() * COMPACTION_FACTOR + MIN_APPENDS_BEFORE_COMPACTION) {
            compactLocked();
        }
    }

    /**
     * Rewrites the log file from the current in-memory state. Closes the current writer,
     * writes and fsyncs a temp file in the same directory, atomically moves it onto the target
     * and reopens the append writer. Package-private so tests can force the writer swap.
     */
    void compact() {
        writeLock.lock();
        try {
            compactLocked();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Compaction body; must be called while holding {@link #writeLock}.
     */
    private void compactLocked() {
        try {
            closeWriter();

            Path temp = Files.createTempFile(directory, file.getFileName().toString(), ".tmp");
            try (Writer out = Files.newBufferedWriter(temp, StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (Map.Entry<String, V> entry : map.entrySet()) {
                    out.write(objectMapper.writeValueAsString(new LogRecord<>(entry.getKey(), entry.getValue())));
                    out.write('\n');
                }
                out.flush();
                fsync(temp);
            }

            moveAtomically(temp, file);
            fsyncDirectory();
            appendsSinceCompaction = 0;
            openWriter();
        } catch (IOException e) {
            reopenWriterQuietly();
            throw new UncheckedIOException("Failed to compact " + file, e);
        }
    }

    /**
     * Opens the append writer on {@link #file}, creating the file if necessary.
     *
     * @throws IOException if the file cannot be opened
     */
    private void openWriter() throws IOException {
        writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    /**
     * Flushes and closes the current writer if open.
     *
     * @throws IOException if closing fails
     */
    private void closeWriter() throws IOException {
        if (writer != null) {
            writer.flush();
            writer.close();
            writer = null;
        }
    }

    /**
     * Best-effort attempt to restore a usable writer after a failed compaction.
     */
    private void reopenWriterQuietly() {
        if (writer == null) {
            try {
                openWriter();
            } catch (IOException reopenError) {
                log.error("Failed to reopen writer for {} after a failed compaction", file, reopenError);
            }
        }
    }

    /**
     * Moves the source onto the target, falling back to a non-atomic replace when the platform
     * does not support atomic moves.
     *
     * @param source the source file
     * @param target the target file
     * @throws IOException if the move fails
     */
    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Flushes file contents to disk.
     *
     * @param path the file to sync
     * @throws IOException if the sync fails
     */
    private void fsync(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    /**
     * Flushes the directory entry so the rename is durable. Not supported on all platforms
     * (e.g. Windows), where it is silently ignored.
     */
    private void fsyncDirectory() {
        try (FileChannel channel = FileChannel.open(directory, StandardOpenOption.READ)) {
            channel.force(true);
        } catch (IOException e) {
            // Directory fsync is not supported on all platforms; ignore.
        }
    }

    /**
     * On-disk record: {@code k} is the key, {@code v} is the value or {@code null} for a
     * tombstone (delete).
     *
     * @param <T> the value type
     */
    static final class LogRecord<T> {

        /** The entry key. */
        public String k;

        /** The entry value, or {@code null} for a tombstone. */
        public T v;

        /** Default constructor for Jackson. */
        public LogRecord() {
        }

        /**
         * Creates a record.
         *
         * @param k the key
         * @param v the value, or {@code null} for a tombstone
         */
        LogRecord(String k, T v) {
            this.k = k;
            this.v = v;
        }
    }
}
