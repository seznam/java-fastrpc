package cz.seznam.frpc.server;

/**
 * Thrown to indicate that a {@code FRPC} method implementation declares a parameter of a type incompatible with current
 * implementation of {@code FRPC} protocol.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class UnsupportedParameterTypeException extends RuntimeException {

    public UnsupportedParameterTypeException() {
    }

    public UnsupportedParameterTypeException(String message) {
        super(message);
    }

    public UnsupportedParameterTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedParameterTypeException(Throwable cause) {
        super(cause);
    }

    protected UnsupportedParameterTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
