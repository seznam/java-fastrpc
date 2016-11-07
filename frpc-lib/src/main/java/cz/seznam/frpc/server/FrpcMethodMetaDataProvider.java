package cz.seznam.frpc.server;

/**
 * An object capable of providing meta data about {@code FRPC} method by its name.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcMethodMetaDataProvider {

    /**
     * Returns an instance of {@link FrpcMethodMetaData} for a {@code FRPC} method of given name or throws an exception
     * if no such method is found.
     *
     * @param methodFrpcName name of the method to return meta data for
     * @return an instance of {@link FrpcMethodMetaData} for a {@code FRPC} method of given name
     * @throws NoSuchMethodException if no method is found for given name
     */
    public FrpcMethodMetaData getMethodMetaData(String methodFrpcName) throws NoSuchMethodException;

}
