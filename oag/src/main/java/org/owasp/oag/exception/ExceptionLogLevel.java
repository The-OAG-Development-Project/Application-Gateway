package org.owasp.oag.exception;

/**
 * Defines the log levels for exceptions in the application.
 * These levels correspond to standard logging levels and determine
 * how exceptions are logged throughout the application.
 */
public enum ExceptionLogLevel {
    /**
     * Trace level - for highly detailed logging, typically used for debugging.
     */
    TRACE,

    /**
     * Debug level - for information useful during development and debugging.
     */
    DEBUG,

    /**
     * Info level - for general information about application progress.
     */
    INFO,

    /**
     * Warning level - for potentially harmful situations that don't prevent normal operation.
     */
    WARNING,

    /**
     * Error level - for errors that prevent normal operation but allow the application to continue.
     */
    ERROR
}
