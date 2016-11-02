package cz.seznam.frpc.client;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class AbstractUnwrappedFrpcCallResult<T> extends AbstractFrpcCallResult<T> {

    protected final Integer statusCode;
    protected final String statusMessage;

    AbstractUnwrappedFrpcCallResult(T wrapped, int httpResponseStatus, Integer statusCode, String statusMessage) {
        super(wrapped, httpResponseStatus);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

}
