package cz.seznam.frpc.server;

/**
 * Thrown to indicate that a {@code FRPC} method implementation has a return type incompatible with current
 * implementation of {@code FRPC} protocol.
 *
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
