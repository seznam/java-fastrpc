package cz.seznam.frpc.core.deserialization;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class EndOfStreamException extends RuntimeException {

    public EndOfStreamException() {
    }

    public EndOfStreamException(String message) {
        super(message);
    }

    public EndOfStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public EndOfStreamException(Throwable cause) {
        super(cause);
    }

    public EndOfStreamException(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
