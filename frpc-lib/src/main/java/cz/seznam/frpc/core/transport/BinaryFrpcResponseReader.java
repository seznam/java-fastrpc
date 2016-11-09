package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.deserialization.FixedLengthFrpcUnmarshaller;
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
                new FixedLengthFrpcUnmarshaller(inputStream, contentLength) : new FrpcUnmarshaller(inputStream);
        // unmarshall one object
        Object response = unmarshaller.unmarshallObject();
        // if the content length is specified, check if there is more data in the stream
        if(contentLength >= 0 && !((FixedLengthFrpcUnmarshaller) unmarshaller).isFinished()) {
            LOGGER.error("Error while reading response data. Content length was specified as {}, and one object was " +
                    "read from the stream, yet there is still data in the stream. Ignoring that data.", contentLength);
        }
        // return the response object
        return response;
    }

}
