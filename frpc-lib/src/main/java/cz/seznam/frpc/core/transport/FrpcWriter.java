package cz.seznam.frpc.core.transport;

import java.io.OutputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcWriter<T> {

    public void write(T object, OutputStream outputStream) throws FrpcTransportException;

}
