package cz.seznam.frpc.core.deserialization;

import org.apache.commons.io.input.CountingInputStream;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class CountingFrpcUnmarshaller extends FrpcUnmarshaller {

    public CountingFrpcUnmarshaller(InputStream inputStream) {
        super(new CountingInputStream(Objects.requireNonNull(inputStream, "Input stream must not be null")));
    }

    public long getBytesRead() {
        return ((CountingInputStream) input).getByteCount();
    }

}
