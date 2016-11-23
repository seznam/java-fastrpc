package cz.seznam.frpc.server;

import java.util.Map;

/**
 * Simple DTO class holding an instance of {@link FrpcHandler} and a map of its methods meta data.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class FrpcMethodHandlerAndMethods {

    private Map<String, FrpcMethodMetaData> methodsMetaData;
    private FrpcHandler frpcHandler;

    FrpcMethodHandlerAndMethods(
            Map<String, FrpcMethodMetaData> methodsMetaData, FrpcHandler frpcHandler) {
        this.methodsMetaData = methodsMetaData;
        this.frpcHandler = frpcHandler;
    }

    Map<String, FrpcMethodMetaData> getMethodsMetaData() {
        return methodsMetaData;
    }

    FrpcHandler getFrpcHandler() {
        return frpcHandler;
    }
}
