package cz.seznam.frpc.core.transport;

import java.io.InputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcReader<T> {

    public T read(InputStream inputStream, long contentLength) throws FrpcTransportException;

}
