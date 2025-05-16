package org.owasp.oag.utils;

/**
 * Provides helper functions to encode data into formats that are not dangerous.
 * This class contains methods for sanitizing strings before they are written to logs
 * or otherwise processed, helping to prevent log injection attacks and other security issues.
 */
public class SecureEncoder {

    /**
     * Encodes the passed in string so that it can be safely added to the log file. This means basically that
     * all non-printable characters are removed, CR/LF is replaced with "CR/LF -" and the size is limited to the number of passed in
     * characters (displaying ... if it is longer)
     *
     * @param inputString the String to sanitize
     * @param maxSize     the max number of characters expected
     * @return the sanitized and (if required shortened) string. Note that maxSize is a soft limit, the shortened String may be slightly longer, depending on the number of newlines in the inputString
     */
    public static String encodeStringForLog(String inputString, int maxSize) {
        if (inputString == null) {
            return null;
        }

        if (inputString.length() > maxSize) {
            return encodeForLog(inputString.substring(0, maxSize - 3) + "...");
        }
        return encodeForLog(inputString);
    }

    /**
     * Internal helper method that performs the actual character encoding for log safety.
     * Replaces newlines and carriage returns with safe alternatives to prevent log forging.
     *
     * @param original The original string to encode
     * @return The encoded string safe for logging
     */
    private static String encodeForLog(String original) {
        // replace cr/lf
        return original.replace("\n", "\n_").replace('\r', '_');
    }

}
