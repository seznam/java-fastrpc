package cz.seznam.frpc.core;

import cz.seznam.frpc.core.transport.FrpcTransportException;

/**
 * Specialization of {@link FrpcTransportException} indicating an error during data processing, either marshalling
 * or unmarshalling.
 */
public class FrpcDataProcessingException extends FrpcTransportException {

    public FrpcDataProcessingException() {
    }

    public FrpcDataProcessingException(String message) {
        super(message);
    }

    public FrpcDataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrpcDataProcessingException(Throwable cause) {
        super(cause);
    }

    public FrpcDataProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
