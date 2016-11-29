package cz.seznam.frpc.client;

/**
 * Runtime exception thrown to indicate general problem with {@code FRPC} method call.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcCallException extends RuntimeException {

    public FrpcCallException() {
    }

    public FrpcCallException(String message) {
        super(message);
    }

    public FrpcCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrpcCallException(Throwable cause) {
        super(cause);
    }

    protected FrpcCallException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
