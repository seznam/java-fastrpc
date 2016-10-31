package cz.seznam.frpc.server;

/**
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
