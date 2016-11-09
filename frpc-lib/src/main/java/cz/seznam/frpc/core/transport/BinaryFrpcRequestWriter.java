package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.FrpcDataException;
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
        try {
            // start content by packing method name
            marshaller.packMagic();
            marshaller.packMethodCall(request.getMethodName());
            // marshall params
            for(Object param : request.getParameters()) {
                marshaller.packItem(param);
            }
        } catch (FrpcDataException e) {
            throw new FrpcTransportException(e);
        }
    }

}
