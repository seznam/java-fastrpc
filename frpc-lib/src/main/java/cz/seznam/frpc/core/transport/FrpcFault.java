package cz.seznam.frpc.core.transport;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcFault {

    private final Integer statusCode;
    private final String statusMessage;

    public FrpcFault(Integer statusCode, String statusMessage) {
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
