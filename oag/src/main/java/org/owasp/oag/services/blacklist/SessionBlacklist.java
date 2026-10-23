package org.owasp.oag.services.blacklist;

import java.io.Closeable;

/**
 * This is an abstract interface for a session blacklist.
 * A session blacklist is used to keep track of invalidated sessions,
 * typically due to logout or security concerns.
 */
public interface SessionBlacklist extends Closeable {

    /**
     * Marks a session identifier as invalidated. Is usually called during logout.
     * The contract is that isInvalidated with the same string must be true for at least the ttl time in seconds.
     * Implementation might keep the identifier on the blacklist for longer.
     *
     * @param identifier The session identifier to invalidate
     * @param ttl The time to live in seconds for the invalidation
     */
    void invalidateSession(String identifier, int ttl);

    /**
     * Checks if a session identifier was invalidated. Must return true if a identifier was not longer ago than the ttl.
     *
     * @param identifier The session identifier to check
     * @return true if the identifier is invalidated, false otherwise
     */
    boolean isInvalidated(String identifier);

    /**
     * Should clean up obsolete entries in the blacklist (See expiration).
     * <p>
     * OAG calls this periodically on a background thread, so an implementation must be safe to call
     * concurrently with {@link #invalidateSession(String, int)} and {@link #isInvalidated(String)},
     * and must not lose an identifier invalidated while it runs.
     */
    void cleanup();
}
