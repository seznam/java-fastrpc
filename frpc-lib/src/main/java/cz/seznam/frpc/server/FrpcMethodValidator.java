package cz.seznam.frpc.server;

import cz.seznam.frpc.core.FrpcTypes;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;


/**
 * Abstract class providing core method validation logic.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class FrpcMethodValidator {

    private class ParameterCheckException extends RuntimeException {
        public ParameterCheckException(String message) {
            super(message);
        }
    }

    /**
     * Does additional validation of the method represented by given name and meta data.
     *
     * @param methodName name of the method to be validated
     * @param methodMetaData meta data of the method to be validated
     *
     * @return instance of {@link FrpcMethodValidationResult} containing result of additional validation of this method
     */
    public abstract FrpcMethodValidationResult doAdditionalValidation(String methodName,
                                                                      FrpcMethodMetaData methodMetaData);

    /**
     * Validates given {@code FRPC} method represented by its name and {@link FrpcMethodMetaData}. The validation
     * process determines whether or not given {@code FRPC} method meets all the requirements imposed by this framework,
     * namely it checks that method name is valid and that all parameter types and return types are supported by this
     * framework. <br />
     * Lastly, {@link #doAdditionalValidation(String, FrpcMethodMetaData)} is called to allow subclasses to validate
     * other aspects of the method. If additional validation does not pass, its result is returned.
     *
     * @param methodName name of the method to be validated
     * @param methodMetaData meta data of the method to be validated
     * @return instance of {@link FrpcMethodValidationResult} containing result of validation of this method
     */
    public final FrpcMethodValidationResult validateFrpcMethod(String methodName, FrpcMethodMetaData methodMetaData) {
        // check that method name is not empty and does not contain dots
        if (StringUtils.isBlank(methodName)) {
            return new FrpcMethodValidationResult(false, "Method name cannot be blank");
        }
        if (methodName.indexOf('.') != -1) {
            return new FrpcMethodValidationResult(false, "Method names within handler cannot contain dots");
        }

        // validate method parameter types
        Type[] types = methodMetaData.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            try {
                validateParameterType(types[i]);
            } catch (ParameterCheckException e) {
                return new FrpcMethodValidationResult(false,
                        "Method parameter #" + (i + 1) + " is invalid - " + e.getMessage());
            }
        }
        // validate return type
        try {
            validateParameterType(methodMetaData.getReturnType());
        } catch (ParameterCheckException e) {
            return new FrpcMethodValidationResult(false,
                    "Method return type is invalid - " + e.getMessage());
        }
        // do additional validation
        FrpcMethodValidationResult additionalValidationResult = doAdditionalValidation(methodName, methodMetaData);
        // if additional validation is null, treat it as OK
        return additionalValidationResult != null && !additionalValidationResult
                .isValid() ? additionalValidationResult : new FrpcMethodValidationResult(true, null);
    }

    private void validateParameterType(Type type) {
        if (type instanceof Class) {
            // check that it is compatible raw type
            if (!FrpcTypes.isSupportedRawType((Class) type)) {
                throw new ParameterCheckException("Class " + type.getTypeName() + " is not supported parameter type");
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = ((ParameterizedType) type);
            // check that the raw type is itself compatible type
            Class rawType = (Class) parameterizedType.getRawType();
            validateParameterType(rawType);
            // get actual type arguments
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            // if it is, it must be either map or collection
            if (FrpcTypes.isSupportedMapType(rawType)) {
                // maps have to have Strings (or Objects) as their keys
                if (typeArguments[0] != String.class && typeArguments[0] != Object.class) {
                    throw new ParameterCheckException(
                            "Maps have to declare either String or Object as type of their keys");
                }
                // validate the other type argument
                validateParameterType(typeArguments[1]);
            } else if (FrpcTypes.isSupportedCollectionType(rawType)) {
                // check the only type argument
                validateParameterType(typeArguments[0]);
            }
        } else if (type instanceof GenericArrayType) {
            // arrays are OK as long as the component type is OK
            validateParameterType(((GenericArrayType) type).getGenericComponentType());
        } else if (type instanceof TypeVariable) {
            throw new ParameterCheckException("Type variables are not supported in parameter types");
        } else if (type instanceof WildcardType) {
            throw new ParameterCheckException("Wildcards are not supported in parameter types");
        } else {
            throw new ParameterCheckException("Unknown Type implementation: " + type.getClass().getCanonicalName());
        }
    }


}
