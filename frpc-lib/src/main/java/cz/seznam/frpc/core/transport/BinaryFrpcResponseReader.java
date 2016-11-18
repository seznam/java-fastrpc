package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.deserialization.CountingFrpcUnmarshaller;
import cz.seznam.frpc.core.deserialization.FrpcUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryFrpcResponseReader implements FrpcResponseReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryFrpcResponseReader.class);

    @Override
    public Object read(InputStream inputStream, long contentLength) throws FrpcTransportException {
        // create unmarshaller, either content length aware one or generic one
        FrpcUnmarshaller unmarshaller = contentLength >= 0 ?
                new CountingFrpcUnmarshaller(inputStream) : new FrpcUnmarshaller(inputStream);
        // unmarshall one object
        Object response = unmarshaller.readResponse();
        // if the content length is specified
        if (contentLength >= 0) {
            // check if we read exactly that many bytes of data
            CountingFrpcUnmarshaller countingFrpcUnmarshaller = (CountingFrpcUnmarshaller) unmarshaller;
            if (countingFrpcUnmarshaller.getBytesRead() != contentLength) {
                LOGGER.error("Content length does not reflect the actual number of bytes of data, content length was" +
                                " specified as {}, yet {} bytes were read from the stream", contentLength,
                        countingFrpcUnmarshaller.getBytesRead());
            }
        }
        // return the response object
        return response;
    }

}
