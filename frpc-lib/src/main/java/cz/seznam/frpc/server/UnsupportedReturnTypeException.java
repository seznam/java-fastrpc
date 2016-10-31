package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class UnsupportedReturnTypeException extends RuntimeException {

    public UnsupportedReturnTypeException() {
    }

    public UnsupportedReturnTypeException(String message) {
        super(message);
    }

    public UnsupportedReturnTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedReturnTypeException(Throwable cause) {
        super(cause);
    }

    public UnsupportedReturnTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
