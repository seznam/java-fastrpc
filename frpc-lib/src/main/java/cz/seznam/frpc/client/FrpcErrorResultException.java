package cz.seznam.frpc.client;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcErrorResultException extends RuntimeException {

    public FrpcErrorResultException() {
    }

    public FrpcErrorResultException(String message) {
        super(message);
    }

    public FrpcErrorResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrpcErrorResultException(Throwable cause) {
        super(cause);
    }

    protected FrpcErrorResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
