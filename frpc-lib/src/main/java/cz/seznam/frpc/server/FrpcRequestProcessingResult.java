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
     * An instance of {@link FrpcMethodMetaData} providing information about the method used to carry out the request.
     * This may be used for instance by {@link FrpcResultTransformer} to produce proper result based on whatever
     * method-specific data it needs.
     */
    private FrpcMethodMetaData methodMetaData;

    /**
     * Creates new instance from given object and result key.
     *
     * @param methodResult result of {@code FRPC} method invocation
     * @param methodMetaData instance of {@link FrpcMethodMetaData} providing information about the method used to
     *                       carry out the request
     */
    public FrpcRequestProcessingResult(Object methodResult, FrpcMethodMetaData methodMetaData) {
        this.methodResult = methodResult;
        this.methodMetaData = methodMetaData;
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
     * Returns an instance of {@link FrpcMethodMetaData} providing information about the method used to carry out
     * the request.
     *
     * @return an instance of {@link FrpcMethodMetaData} as described above
     */
    public FrpcMethodMetaData getMethodMetaData() {
        return methodMetaData;
    }

}
