package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
interface MethodNameToParameterTypesMapper {

    public Class<?>[] mapToParameterTypes(String methodName) throws NoSuchMethodException;

}
