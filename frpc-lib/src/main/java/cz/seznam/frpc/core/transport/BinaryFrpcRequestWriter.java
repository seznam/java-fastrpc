package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.serialization.FrpcMarshaller;

import java.io.OutputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryFrpcRequestWriter implements FrpcRequestWriter {

    @Override
    public void write(FrpcRequest request, OutputStream outputStream) throws FrpcTransportException {
        // create marshaller
        FrpcMarshaller marshaller = new FrpcMarshaller(outputStream);
        // write the request
        marshaller.writeRequest(request);
    }

}
