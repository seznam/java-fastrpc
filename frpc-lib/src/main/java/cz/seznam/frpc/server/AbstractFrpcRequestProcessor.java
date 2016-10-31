package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcBinUnmarshaller;
import org.apache.commons.lang3.ClassUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class AbstractFrpcRequestProcessor implements FrpcRequestProcessor {

    protected Object[] unmarshallMethodArguments(String requestMethodName, Class<?>[] methodParameterTypes, FrpcBinUnmarshaller unmarshaller) {
        // try to unmarshall as many objects as there are argument types
        Object[] arguments = new Object[methodParameterTypes.length];
        for(int i = 0; i < arguments.length; i++) {
            // try to unmarshall one parameter
            Object argument;
            try {
                argument = unmarshaller.unmarshallObject();
            } catch (Exception e) {
                throw new IllegalStateException(getMethodDescription(requestMethodName, methodParameterTypes) + " There was an error while reading parameter #" + (i + 1), e);
            }
            // if the argument is null, just store it in the arguments array
            if(argument == null) {
                arguments[i] = null;
            } else {
                // otherwise try to convert the argument into something compatible with current parameter type
                Object convertedArgument = convertToCompatibleInstance(methodParameterTypes[i], argument);
                if(convertedArgument == null) {
                    throw new IllegalArgumentException(
                            "Error while reading method arguments. " + getMethodDescription(requestMethodName, methodParameterTypes)
                                    + " Argument no. " + (i + 1) + " is then expected to be " + methodParameterTypes[i].getSimpleName() + " but an object of type "
                                    + argument.getClass().getSimpleName() + " was given");
                }
                // add it to the array of parameters
                arguments[i] = convertedArgument;
            }
        }
        // return unmarshalled arguments
        return arguments;
    }

    protected String getMethodDescription(String methodName, Class<?>[] parameterTypes) {
        return "Method \"" + methodName + "\" is mapped to handler method with parameters of types "
                + Arrays.stream(parameterTypes).map(Class::getSimpleName).collect(Collectors.joining(",", "[", "]"))
                + ".";
    }

    protected Object convertToCompatibleInstance(Class<?> methodParameterType, Object argument) {
        // get boxed method parameter type
        Class<?> boxedMethodParameterType = ClassUtils.primitiveToWrapper(methodParameterType);
        // check if argument is instance of given type or if they are compatible numbers
        if(boxedMethodParameterType.isInstance(argument)) {
            return argument;
        }
        // get boxed type of the argument
        Class<?> boxedArgumentType = ClassUtils.primitiveToWrapper(argument.getClass());
        // check for compatible integer and floating point types
        if((boxedMethodParameterType == Long.class && boxedArgumentType == Integer.class)
                || (boxedMethodParameterType == Double.class && boxedArgumentType == Float.class)) {
            return argument;
        }
        // if the method parameter type is a list and argument is an array, convert it into list
        if(boxedMethodParameterType == List.class && boxedArgumentType == Object[].class) {
            return Arrays.stream((Object[]) argument).collect(Collectors.toList());
        }
        // if the method parameter type is a list and argument is an array, convert it into list
        if(boxedMethodParameterType == Set.class && boxedArgumentType == Object[].class) {
            return Arrays.stream((Object[]) argument).collect(Collectors.toSet());
        }
        // if none of the above is true, given argument is not compatible with given type
        return null;
    }

}
