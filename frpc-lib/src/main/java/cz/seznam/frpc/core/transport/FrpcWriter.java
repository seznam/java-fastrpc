package cz.seznam.frpc.core.transport;

import java.io.OutputStream;

/**
 * {@code FRPC} reader is capable of writing objects into given output stream.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcWriter<T> {

    /**
     * Writes given object into the stream.
     *
     * @param object object to be written into the stream
     * @param outputStream stream to write the object into
     * @throws FrpcTransportException if anything goes wrong while writing the object into the stream
     */
    public void write(T object, OutputStream outputStream) throws FrpcTransportException;

}
