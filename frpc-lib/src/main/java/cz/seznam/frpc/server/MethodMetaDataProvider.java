package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
interface MethodMetaDataProvider {

    public MethodMetaData getMethodMetaData(String methodName) throws NoSuchMethodException;

}
