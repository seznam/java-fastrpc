package cz.seznam.frpc.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simple DTO class holding an instance of {@link FrpcHandler} and a map of its method names to corresponding meta data.
 * This class is fully immutable.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class FrpcMethodHandlerAndMethods {

    /**
     * Mapping from method names to corresponding meta data.
     */
    private Map<String, FrpcMethodMetaData> methodsMetaData;
    /**
     * The handler methods above belong to.
     */
    private FrpcHandler frpcHandler;

    /**
     * Creates new instance from given arguments.
     *
     * @param methodsMetaData mapping from {@code FRPC} method names to corresponding meta data
     * @param frpcHandler the handler {@code methodsMetaData} belong to
     */
    FrpcMethodHandlerAndMethods(
            Map<String, FrpcMethodMetaData> methodsMetaData, FrpcHandler frpcHandler) {
        this.methodsMetaData = Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(methodsMetaData)));
        this.frpcHandler = Objects.requireNonNull(frpcHandler);
    }

    /**
     * Returns an unmodifiable view of the mapping from method names to their meta data.
     *
     * @return an unmodifiable view of the mapping from method names to their meta data
     */
    Map<String, FrpcMethodMetaData> getMethodsMetaData() {
        return methodsMetaData;
    }

    /**
     * Returns the handler.
     *
     * @return the handler
     */
    FrpcHandler getFrpcHandler() {
        return frpcHandler;
    }
}
