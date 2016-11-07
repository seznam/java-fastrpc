package cz.seznam.frpc.server;

/**
 * Immutable DTO class holding result of {@code FRPC} method invocation and string key (which is possibly null) under
 * which to store the result in the response map.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcRequestProcessingResult {

    /**
     * Anything, result of the actual {@code FRPC} method call.
     */
    private Object methodResult;
    /**
     * Possibly null (if the methodResult is a map). Specifies under which key the result will be stored in
     * the result map.
     */
    private String methodResponseKey;

    /**
     * Creates new instance from given object and result key.
     *
     * @param methodResult result of {@code FRPC} method invocation
     * @param methodResponseKey key (possibly null) under which to store the result in the response map
     */
    public FrpcRequestProcessingResult(Object methodResult, String methodResponseKey) {
        this.methodResult = methodResult;
        this.methodResponseKey = methodResponseKey;
    }

    /**
     * Returns the result of {@code FRPC} method invocation.
     *
     * @return result of {@code FRPC} method invocation
     */
    public Object getMethodResult() {
        return methodResult;
    }

    /**
     * Returns the key under which to store the result in the response map.
     *
     * @return key (possibly null) under which to store the result in the response map
     */
    public String getMethodResponseKey() {
        return methodResponseKey;
    }

}
