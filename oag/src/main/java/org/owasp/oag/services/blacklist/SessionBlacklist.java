package org.owasp.oag.services.blacklist;

import reactor.core.publisher.Mono;

import java.io.Closeable;

/**
 * This is an abstract interface for a session blacklist.
 */
public interface SessionBlacklist extends Closeable {

    /**
     * Marks a session identifier as invalidated. Is usually called during logout.
     * The contract is that isInvalidated with the same string must be true for at least the ttl time in seconds.
     * Implementation might keep the identifier on the blacklist for longer.
     *
     * @param identifier
     * @param ttl
     */
    Mono<Void> invalidateSession(String identifier, int ttl);

    /**
     * Checks if a session identifier was invalidated. Must return true if a identifier was not longer ago than the ttl.
     *
     * @param identifier
     * @return
     */
    Mono<Boolean> isInvalidated(String identifier);

    /**
     * Should clean up obsolete entries in the blacklist (See expiration)
     * Can be regularly called by the runtime.
     */
    Mono<Void> cleanup();
}
