package cz.seznam.frpc.core.deserialization;

import org.apache.commons.io.input.CountingInputStream;

import java.io.InputStream;
import java.util.Objects;

/**
 * Extension of {@link FrpcUnmarshaller} wrapping given stream in {@link CountingInputStream} so that it can report
 * the number of bytes read from the stream. Can come in handy when content length is needed yet you don't want to
 * keep all the deserialized data in memory using {@link java.io.ByteArrayInputStream}.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class CountingFrpcUnmarshaller extends FrpcUnmarshaller {

    /**
     * Wraps given stream into {@link CountingInputStream} and calls {@link FrpcUnmarshaller#FrpcUnmarshaller(InputStream)}
     *
     * @param inputStream stream to wrap
     */
    public CountingFrpcUnmarshaller(InputStream inputStream) {
        super(new CountingInputStream(Objects.requireNonNull(inputStream, "Input stream must not be null")));
    }

    /**
     * Returns the number of bytes written into the stream since this {@code CountingFrpcUnmarshaller} was created.
     *
     * @return number of bytes written into the stream since this {@code CountingFrpcUnmarshaller} was created
     */
    public long getBytesRead() {
        return ((CountingInputStream) input).getByteCount();
    }

}
