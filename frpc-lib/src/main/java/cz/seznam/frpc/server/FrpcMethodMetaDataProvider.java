package cz.seznam.frpc.server;

/**
 * An object capable of providing meta data about {@code FRPC} method by its value.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcMethodMetaDataProvider {

    /**
     * Returns an instance of {@link FrpcMethodMetaData} for a {@code FRPC} method of given value or throws an exception
     * if no such method is found.
     *
     * @param methodFrpcName value of the method to return meta data for
     * @return an instance of {@link FrpcMethodMetaData} for a {@code FRPC} method of given value
     * @throws NoSuchMethodException if no method is found for given value
     */
    public FrpcMethodMetaData getMethodMetaData(String methodFrpcName) throws NoSuchMethodException;

}
