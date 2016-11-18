package cz.seznam.frpc.server;

import cz.seznam.frpc.core.transport.FrpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class HandlerUsingFrpcRequestProcesor implements FrpcRequestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerUsingFrpcRequestProcesor.class);

    private static final String DEFAULT_HANDLER_NAME = "";

    private Map<String, FrpcMethodMetaDataProviderAndHandler> handlerMapping;

    public HandlerUsingFrpcRequestProcesor(FrpcHandlerMapping handlerMapping) {
        Objects.requireNonNull(handlerMapping);
        this.handlerMapping = handlerMapping.getMapping();
    }

    @Override
    public FrpcRequestProcessingResult process(FrpcRequest frpcRequest) throws Exception {
        // get request method name
        String requestMethodName = frpcRequest.getMethodName();
        LOGGER.debug("Unmarshalled FRPC method name: {}", requestMethodName);
        // check if there is a dot somewhere in the method name
        int lastDotIndex = requestMethodName.lastIndexOf('.');

        String handlerName;
        String handlerMethodName;
        // if there is no dot
        if(lastDotIndex == -1) {
            // handler is not specified and the whole unmarshalled method name is the name of a method
            handlerName = DEFAULT_HANDLER_NAME;
            handlerMethodName = requestMethodName;
        } else if(lastDotIndex == 0 || lastDotIndex == requestMethodName.length() - 1) {
            throw new IllegalArgumentException("FRPC method name must not start or end with a dot");
        } else {
            // otherwise everything up to the last dot is the handler name
            handlerName = requestMethodName.substring(0, lastDotIndex);
            // and the rest is the actual method name
            handlerMethodName = requestMethodName.substring(lastDotIndex + 1, requestMethodName.length());
        }
        LOGGER.debug("Delegating FRPC method call to method \"{}\" of handler mapped to \"{}\"", handlerMethodName,
                handlerName);

        // invoke the method
        return invokeHandler(handlerName, handlerMethodName, requestMethodName, frpcRequest.getParametersAsArray());
    }

    private FrpcRequestProcessingResult invokeHandler(String handlerName, String handlerMethodName,
                                                      String requestMethodName, Object[] parameters)
            throws Exception {
        // try to find the handler first
        FrpcMethodMetaDataProviderAndHandler methodHandler = handlerMapping.get(handlerName);
        // check if we have any handler mapped to this name
        if(methodHandler == null) {
            throw new RuntimeException("There is no handler mapped to prefix \"" + handlerName + "\"");
        }

        // get the method argument types resolver
        FrpcMethodMetaDataProvider methodMetaDataProvider = methodHandler.getMethodMetaDataProvider();
        // get method metadata
        FrpcMethodMetaData methodMetaData = methodMetaDataProvider.getMethodMetaData(handlerMethodName);
        Class<?>[] parameterTypes = methodMetaData.getParameterTypes();

        // try to unmarshall arguments according to method parameter types
        Object[] arguments = FrpcServerUtils
                .checkAndConvertMethodParameters(requestMethodName, parameterTypes, parameters);

        // call the method handler
        Object methodResult = methodHandler.getFrpcHandler().handleFrpcMethodCall(handlerMethodName, arguments);
        // create proper object to return
        return new FrpcRequestProcessingResult(methodResult, methodMetaData.getResponseKey());
    }

}
