package ch.gianlucafrei.nellygateway.utils;

/**
 * Provides helper functions to encode data into formats that are not dangerous.
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

    private static String encodeForLog(String original) {
        // replace cr/lf
        return original.replace("\n", "\n_").replace('\r', '_');
    }

}
