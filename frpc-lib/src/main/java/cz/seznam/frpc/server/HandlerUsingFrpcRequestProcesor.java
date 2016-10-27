package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcBinUnmarshaller;
import org.apache.commons.lang3.ClassUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class HandlerUsingFrpcRequestProcesor implements FrpcRequestProcessor {

    private static final String DEFAULT_HANDLER_NAME = "";

    private Map<String, FrpcMethodHandler> handlerMapping;

    public HandlerUsingFrpcRequestProcesor(FrpcHandlerMapping handlerMapping) {
        Objects.requireNonNull(handlerMapping);
        this.handlerMapping = handlerMapping.getMapping();
    }

    @Override
    public FrpcRequestProcessingResult process(InputStream is) throws Exception {
        // create FRPC unmarshaller
        FrpcBinUnmarshaller unmarshaller = new FrpcBinUnmarshaller(is);

        // try to unmarshall method name
        String requestMethodName = unmarshaller.unmarshallMethodName();
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

        // invoke the method
        return invokeHandler(handlerName, handlerMethodName, requestMethodName, unmarshaller);
    }

    private FrpcRequestProcessingResult invokeHandler(String handlerName, String handlerMethodName, String requestMethodName, FrpcBinUnmarshaller unmarshaller)
            throws Exception {
        // try to find the handler first
        FrpcMethodHandler methodHandler = handlerMapping.get(handlerName);
        // check if we have any handler mapped to this name
        if(methodHandler == null) {
            throw new RuntimeException("There is no handler mapped to prefix \"" + handlerName + "\"");
        }

        // get the method argument types resolver
        MethodMetaDataProvider methodMetaDataProvider = methodHandler.getMethodMetaDataProvider();
        // get method metadata
        MethodMetaData methodMetaData = methodMetaDataProvider.getMethodMetaData(handlerMethodName);
        Class<?>[] parameterTypes = methodMetaData.getParameterTypes();

        // try to unmarshall as many objects as there are argument types
        Object[] arguments = new Object[parameterTypes.length];
        for(int i = 0; i < arguments.length; i++) {
            // try to unmarshall one parameter
            Object argument;
            try {
                argument = unmarshaller.unmarshallObject();
            } catch (Exception e) {
                throw new IllegalStateException(getMethodDescription(requestMethodName, parameterTypes) + " There was an error while reading parameter #" + (i + 1), e);
            }
            // check its type
            // TODO: handle lower-precision numbers properly
            if(!ClassUtils.primitiveToWrapper(parameterTypes[i]).isInstance(argument)) {
                throw new IllegalArgumentException(
                        "Error while reading method arguments. " + getMethodDescription(requestMethodName, parameterTypes)
                                + " Argument no. " + (i + 1) + " is then expected to be " + parameterTypes[i].getSimpleName() + " but an object of type "
                                + argument.getClass().getSimpleName() + " was given");
            }
            // add it to the array of parameters
            arguments[i] = argument;
        }

        // call the method handler
        Object methodResult = methodHandler.getFrpcHandler().handleRequest(handlerMethodName, arguments);
        // create proper object to return
        return new FrpcRequestProcessingResult(methodResult, methodMetaData.getResponseKey());
    }

    private String getMethodDescription(String methodName, Class<?>[] parameterTypes) {
        return "Method \"" + methodName + "\" is mapped to handler method with parameters of types "
                + Arrays.stream(parameterTypes).map(Class::getSimpleName).collect(Collectors.joining(",", "[", "]"))
                + ".";
    }

}
