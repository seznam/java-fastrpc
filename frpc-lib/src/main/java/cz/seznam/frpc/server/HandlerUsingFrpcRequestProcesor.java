package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class HandlerUsingFrpcRequestProcesor extends AbstractFrpcRequestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerUsingFrpcRequestProcesor.class);

    private static final String DEFAULT_HANDLER_NAME = "";

    private Map<String, FrpcMethodHandler> handlerMapping;

    public HandlerUsingFrpcRequestProcesor(FrpcHandlerMapping handlerMapping) {
        Objects.requireNonNull(handlerMapping);
        this.handlerMapping = handlerMapping.getMapping();
    }

    @Override
    public FrpcRequestProcessingResult process(InputStream is) throws Exception {
        // create FRPC unmarshaller
        FrpcUnmarshaller unmarshaller = new FrpcUnmarshaller(is);

        // try to unmarshall method name
        String requestMethodName = unmarshaller.unmarshallMethodName();
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
        LOGGER.debug("Delegating FRPC method call to method \"{}\" of handler mapped to \"{}\"", handlerMethodName, handlerName);

        // invoke the method
        return invokeHandler(handlerName, handlerMethodName, requestMethodName, unmarshaller);
    }

    private FrpcRequestProcessingResult invokeHandler(String handlerName, String handlerMethodName, String requestMethodName, FrpcUnmarshaller unmarshaller)
            throws Exception {
        // try to find the handler first
        FrpcMethodHandler methodHandler = handlerMapping.get(handlerName);
        // check if we have any handler mapped to this name
        if(methodHandler == null) {
            throw new RuntimeException("There is no handler mapped to prefix \"" + handlerName + "\"");
        }

        // getResult the method argument types resolver
        MethodMetaDataProvider methodMetaDataProvider = methodHandler.getMethodMetaDataProvider();
        // getResult method metadata
        MethodMetaData methodMetaData = methodMetaDataProvider.getMethodMetaData(handlerMethodName);
        Class<?>[] parameterTypes = methodMetaData.getParameterTypes();

        // try to unmarshall arguments according to method parameter types
        Object[] arguments = unmarshallMethodArguments(requestMethodName, parameterTypes, unmarshaller);

        // call the method handler
        Object methodResult = methodHandler.getFrpcHandler().handleRequest(handlerMethodName, arguments);
        // create proper object to return
        return new FrpcRequestProcessingResult(methodResult, methodMetaData.getResponseKey());
    }

}
