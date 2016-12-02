package cz.seznam.frpc.server;

import cz.seznam.frpc.core.FrpcTypesConverter;
import cz.seznam.frpc.core.transport.FrpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link FrpcRequestProcessor} using {@link FrpcHandlerMapping} to delegate {@code FRPC} request to
 * proper methods of proper handlers.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class HandlerUsingFrpcRequestProcesor implements FrpcRequestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerUsingFrpcRequestProcesor.class);

    private static final String DEFAULT_HANDLER_NAME = "";

    private Map<String, FrpcMethodHandlerAndMethods> handlerMapping;

    /**
     * Creates new instance from given argument.
     *
     * @param handlerMapping handler mapping to use when delegating {@code FRPC} request to proper methods of proper
     *                       handlers
     */
    public HandlerUsingFrpcRequestProcesor(FrpcHandlerMapping handlerMapping) {
        Objects.requireNonNull(handlerMapping);
        this.handlerMapping = handlerMapping.getMapping();
    }

    @Override
    public FrpcRequestProcessingResult process(FrpcRequest frpcRequest) throws Exception {
        // get request method value
        String fullMethodName = frpcRequest.getMethodName();
        LOGGER.debug("Unmarshalled FRPC method value: {}", fullMethodName);
        // check if there is a dot somewhere in the method value
        int lastDotIndex = fullMethodName.lastIndexOf('.');

        String handlerName;
        String handlerMethodName;
        // if there is no dot
        if (lastDotIndex == -1) {
            // handler is not specified and the whole unmarshalled method value is the value of a method
            handlerName = DEFAULT_HANDLER_NAME;
            handlerMethodName = fullMethodName;
        } else if (lastDotIndex == 0 || lastDotIndex == fullMethodName.length() - 1) {
            throw new IllegalArgumentException("FRPC method value must not start or end with a dot");
        } else {
            // otherwise everything up to the last dot is the handler value
            handlerName = fullMethodName.substring(0, lastDotIndex);
            // and the rest is the actual method value
            handlerMethodName = fullMethodName.substring(lastDotIndex + 1, fullMethodName.length());
        }
        LOGGER.debug("Delegating FRPC method call to method \"{}\" of handler mapped to \"{}\"", handlerMethodName,
                handlerName);

        // invoke the method
        return invokeHandler(handlerName, handlerMethodName, fullMethodName, frpcRequest.getParametersAsArray());
    }

    private FrpcRequestProcessingResult invokeHandler(String handlerName, String handlerMethodName,
                                                      String fullMethodName, Object[] parameters)
            throws Exception {
        // try to find the handler first
        FrpcMethodHandlerAndMethods methodHandler = handlerMapping.get(handlerName);
        // check if we have any handler mapped to this value
        if (methodHandler == null) {
            throw new RuntimeException("There is no handler mapped to prefix \"" + handlerName + "\"");
        }

        // get the method argument types resolver
        Map<String, FrpcMethodMetaData> metaDataByMethodNames = methodHandler.getMethodsMetaData();
        // check that methods contain current method name
        if (!metaDataByMethodNames.containsKey(handlerMethodName)) {
            throw new NoSuchMethodException(
                    "No FRPC found for name \"" + handlerMethodName + "\" within handler mapped to \"" + handlerName + "\"");
        }
        // get method metadata
        FrpcMethodMetaData methodMetaData = metaDataByMethodNames.get(handlerMethodName);
        Type[] parameterTypes = methodMetaData.getParameterTypes();

        // try to unmarshall arguments according to method parameter types
        Object[] arguments = FrpcTypesConverter
                .checkAndConvertMethodParameters(fullMethodName, parameterTypes, parameters);

        // call the method handler
        Object methodResult = methodHandler.getFrpcHandler().handleFrpcMethodCall(handlerMethodName, arguments);
        // create proper object to return
        return new FrpcRequestProcessingResult(methodResult, methodMetaData);
    }

}
