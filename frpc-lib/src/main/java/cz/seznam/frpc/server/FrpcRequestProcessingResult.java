package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcRequestProcessingResult {

    private Object methodResult;
    private String methodResponseKey;

    public FrpcRequestProcessingResult(Object methodResult, String methodResponseKey) {
        this.methodResult = methodResult;
        this.methodResponseKey = methodResponseKey;
    }

    public Object getMethodResult() {
        return methodResult;
    }

    public String getMethodResponseKey() {
        return methodResponseKey;
    }

}
