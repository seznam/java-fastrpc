package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class RequestProcessingException extends Exception {

    public RequestProcessingException() {
    }

    public RequestProcessingException(String message) {
        super(message);
    }

    public RequestProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestProcessingException(Throwable cause) {
        super(cause);
    }

}
