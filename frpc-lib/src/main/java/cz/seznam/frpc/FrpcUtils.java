package cz.seznam.frpc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcUtils {

    public static final String STATUS_KEY = "status";
    public static final String STATUS_MESSAGE_KEY = "statusMessage";
    public static final String DEFAULT_OK_STATUS_MESSAGE = "OK";
    public static final String DEFAULT_ERROR_STATUS_MESSAGE = "ERROR";

    public static Map<String, Object> ok() {
        return response(20, DEFAULT_OK_STATUS_MESSAGE);
    }

    public static Map<String, Object> ok(String statusMessage) {
        return response(20, statusMessage);
    }

    public static Map<String, Object> error() {
        return response(500, DEFAULT_ERROR_STATUS_MESSAGE);
    }

    public static Map<String, Object> error(String statusMessage) {
        return response(500, statusMessage);
    }

    public static Map<String, Object> wrapException(Exception exception) {
        // serialize stack trace into one string
        String statusMessage = exception.getMessage() + "\n" +
                Arrays.stream(exception.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
        // return error containing this status message
        return error(statusMessage);
    }

    private static Map<String, Object> response(int statusCode, String statusMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put(STATUS_KEY, statusCode);
        response.put(STATUS_MESSAGE_KEY, statusMessage);
        return response;
    }

}
