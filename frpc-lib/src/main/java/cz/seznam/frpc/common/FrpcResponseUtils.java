package cz.seznam.frpc.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Collection of utility methods and constants simplifying creation of response objects of type {@code Map}.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcResponseUtils {

    /**
     * Default key to store status code under in response map.
     */
    public static final String STATUS_KEY = "status";
    /**
     * Default key to store status message under in response map.
     */
    public static final String STATUS_MESSAGE_KEY = "statusMessage";
    /**
     * Default OK status message.
     */
    public static final int OK_STATUS_CODE = 200;
    /**
     * Default error status message.
     */
    public static final int ERROR_STATUS_CODE = 500;
    /**
     * Default OK status message.
     */
    public static final String DEFAULT_OK_STATUS_MESSAGE = "OK";
    /**
     * Default error status message.
     */
    public static final String DEFAULT_ERROR_STATUS_MESSAGE = "ERROR";

    /**
     * Convenience method for calling {@link #response(int, String)} with {@link #OK_STATUS_CODE} and
     * {@link #DEFAULT_OK_STATUS_MESSAGE}.
     *
     * @return a map containing OK response
     */
    public static Map<String, Object> ok() {
        return response(OK_STATUS_CODE, DEFAULT_OK_STATUS_MESSAGE);
    }

    /**
     * Convenience method for calling {@link #response(int, String)} with {@link #OK_STATUS_CODE} and
     * given status message.
     *
     * @return a map containing OK response
     */
    public static Map<String, Object> ok(String statusMessage) {
        return response(OK_STATUS_CODE, statusMessage);
    }

    /**
     * Convenience method for calling {@link #response(int, String)} with {@link #ERROR_STATUS_CODE} and
     * {@link #DEFAULT_ERROR_STATUS_MESSAGE}.
     *
     * @return a map containing error response
     */
    public static Map<String, Object> error() {
        return response(500, DEFAULT_ERROR_STATUS_MESSAGE);
    }

    /**
     * Convenience method for calling {@link #response(int, String)} with {@link #ERROR_STATUS_CODE} and
     * given status message.
     *
     * @return a map containing error response
     */
    public static Map<String, Object> error(String statusMessage) {
        return response(ERROR_STATUS_CODE, statusMessage);
    }

    /**
     * Convenience method for calling {@link #response(int, String)} with {@link #ERROR_STATUS_CODE} and
     * status message being a string representation of stack trace of given exception;
     *
     * @return a map containing error response
     */
    public static Map<String, Object> error(Exception exception) {
        StringWriter sw = new StringWriter();
        // print stacktrace into the StringWriter
        exception.printStackTrace(new PrintWriter(sw));
        // return error containing this status message
        return error(sw.toString());
    }

    /**
     * Returns a map containing two mappings: <br />
     * <ul>
     *     <li>
     *         {@link #STATUS_KEY} to given {@code statusCode} and
     *     </li>
     *     <li>
     *         {@link #STATUS_MESSAGE_KEY} to given {@code statusMessage}
     *     </li>
     * </ul>
     *
     * @param statusCode status code to be stored under {@link #STATUS_KEY}
     * @param statusMessage status message to be stored under {@link #STATUS_MESSAGE_KEY}
     *
     * @return a map as described above
     */
    public static Map<String, Object> response(int statusCode, String statusMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put(STATUS_KEY, statusCode);
        response.put(STATUS_MESSAGE_KEY, statusMessage);
        return response;
    }

}
