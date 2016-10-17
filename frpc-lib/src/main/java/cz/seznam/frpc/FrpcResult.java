package cz.seznam.frpc;

/**
 * Class representing standard result of frpc call.
 * 
 * 
 * @author Jakub Janda
 * 
 */
public class FrpcResult {

    public enum FrpcResultStatus {
        RESULT_OK, RESULT_NETWORK_ERROR, RESULT_DATA_ERROR
    }

    private final FrpcStruct data;

    private final FrpcResultStatus status;

    public FrpcResult(FrpcStruct data, FrpcResultStatus status) {
        this.data = data;
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("FrpcResult {status=").append(status).append("; data=");

        if (data != null) {
            sb.append("\n").append(data.toString());
        } else {
            sb.append("null");
        }

        sb.append("}");

        return sb.toString();
    }
}
