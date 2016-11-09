package cz.seznam.frpc.core.transport;

/**
 * General-purpose runtime exception to indicate any lower-level (transport) problems.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcTransportException extends RuntimeException {

    public FrpcTransportException() {
    }

    public FrpcTransportException(String message) {
        super(message);
    }

    public FrpcTransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrpcTransportException(Throwable cause) {
        super(cause);
    }

    public FrpcTransportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
