package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.deserialization.CountingFrpcUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryFrpcRequestReader implements FrpcRequestReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryFrpcRequestReader.class);

    @Override
    public FrpcRequest read(InputStream inputStream, long contentLength) throws FrpcTransportException {
        Objects.requireNonNull(inputStream, "Input stream must not be null");
        // create unmarshaller
        CountingFrpcUnmarshaller unmarshaller = new CountingFrpcUnmarshaller(inputStream);
        // read the request
        FrpcRequest frpcRequest = unmarshaller.readRequest();
        // if the content length is specified
        if (contentLength >= 0) {
            // check if we read exactly that many bytes of data
            if (unmarshaller.getBytesRead() != contentLength) {
                LOGGER.error("Content length does not reflect the actual number of bytes of data, content length was" +
                                " specified as {}, yet {} bytes were read from the stream", contentLength,
                        unmarshaller.getBytesRead());
            }
        }
        // and return it
        return frpcRequest;
    }

}
