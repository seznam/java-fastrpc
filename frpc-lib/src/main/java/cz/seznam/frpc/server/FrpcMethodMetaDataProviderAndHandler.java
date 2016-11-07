package cz.seznam.frpc.server;

/**
 * Simple DTO class holding an instance of {@link FrpcMethodMetaDataProvider} and its corresponding {@link FrpcHandler}.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class FrpcMethodMetaDataProviderAndHandler {

    private FrpcMethodMetaDataProvider methodMetaDataProvider;
    private FrpcHandler frpcHandler;

    FrpcMethodMetaDataProviderAndHandler(FrpcMethodMetaDataProvider methodMetaDataProvider, FrpcHandler frpcHandler) {
        this.methodMetaDataProvider = methodMetaDataProvider;
        this.frpcHandler = frpcHandler;
    }

    FrpcMethodMetaDataProvider getMethodMetaDataProvider() {
        return methodMetaDataProvider;
    }

    FrpcHandler getFrpcHandler() {
        return frpcHandler;
    }

}
