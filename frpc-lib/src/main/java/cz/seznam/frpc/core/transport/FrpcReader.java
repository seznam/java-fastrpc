package cz.seznam.frpc.core.transport;

import java.io.InputStream;

/**
 * {@code FRPC} reader is capable of reading objects from given input stream.
 *
 * @param <T> type of objects this reader can read from the stream
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcReader<T> {

    /**
     * Reads object of specified type from given input stream, optionally using given content length. Implementations
     * are free to ignore the content length.
     *
     * @param inputStream input stream to read data from
     * @param contentLength content length, negative value means the content length is unknown
     * @return an object read from the stream
     * @throws FrpcTransportException if anything goes wrong while reading the object from the input stream
     */
    public T read(InputStream inputStream, long contentLength) throws FrpcTransportException;

}
