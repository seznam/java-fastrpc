package cz.seznam.frpc.server;

import cz.seznam.frpc.core.FrpcTypes;

import java.lang.reflect.*;


/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class FrpcMethodValidator {

    private class ParameterCheckException extends RuntimeException {
        public ParameterCheckException(String message) {
            super(message);
        }
    }

    public abstract FrpcMethodValidationResult doAdditionalValidation(FrpcMethodMetaData methodMetaData);

    public final FrpcMethodValidationResult validateFrpcMethod(FrpcMethodMetaData methodMetaData) {
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
        FrpcMethodValidationResult additionalValidationResult = doAdditionalValidation(methodMetaData);
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
        } else if(type instanceof GenericArrayType) {
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
