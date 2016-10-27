package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class FrpcMethodHandler {

    private MethodMetaDataProvider methodMetaDataProvider;
    private FrpcHandler frpcHandler;

    public FrpcMethodHandler(MethodMetaDataProvider methodMetaDataProvider, FrpcHandler frpcHandler) {
        this.methodMetaDataProvider = methodMetaDataProvider;
        this.frpcHandler = frpcHandler;
    }

    public MethodMetaDataProvider getMethodMetaDataProvider() {
        return methodMetaDataProvider;
    }

    public FrpcHandler getFrpcHandler() {
        return frpcHandler;
    }

}
