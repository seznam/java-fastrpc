package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.deserialization.FixedLengthFrpcUnmarshaller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryFrpcRequestReader implements FrpcRequestReader {

    @Override
    public FrpcRequest read(InputStream inputStream, long contentLength) throws FrpcTransportException {
        Objects.requireNonNull(inputStream, "Input stream must not be null");
        // create unmarshaller
        FixedLengthFrpcUnmarshaller unmarshaller = new FixedLengthFrpcUnmarshaller(inputStream, contentLength);
        // unmarshall method name
        String methodName = unmarshaller.unmarshallMethodName();
        // unmarshall objects while there are any
        List<Object> parameters = new ArrayList<>(25);
        while(!unmarshaller.isFinished()) {
            parameters.add(unmarshaller.unmarshallObject());
        }
        // construct new FrpcRequest
        FrpcRequest frpcRequest = new FrpcRequest(methodName, parameters);
        // and return it
        return frpcRequest;
    }

}
