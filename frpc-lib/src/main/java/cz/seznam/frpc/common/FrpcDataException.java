package cz.seznam.frpc.common;

public class FrpcDataException extends Exception {

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
