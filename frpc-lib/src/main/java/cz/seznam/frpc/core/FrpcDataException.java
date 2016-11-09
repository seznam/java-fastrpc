package cz.seznam.frpc.core;

import cz.seznam.frpc.core.transport.FrpcTransportException;

public class FrpcDataException extends FrpcTransportException {

    public FrpcDataException() {
    }

    public FrpcDataException(String message) {
        super(message);
    }

    public FrpcDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrpcDataException(Throwable cause) {
        super(cause);
    }

    public FrpcDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
