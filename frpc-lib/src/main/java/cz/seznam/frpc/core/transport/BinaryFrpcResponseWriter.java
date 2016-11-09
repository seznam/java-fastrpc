package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.FrpcDataException;
import cz.seznam.frpc.core.serialization.FrpcMarshaller;

import java.io.OutputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryFrpcResponseWriter implements FrpcResponseWriter {

    @Override
    public void write(Object response, OutputStream outputStream) throws FrpcTransportException {
        FrpcMarshaller marshaller = new FrpcMarshaller(outputStream);
        try {
            marshaller.packMagic();
            marshaller.packItem(response);
        } catch (FrpcDataException e) {
            throw new FrpcTransportException("Error while writing FRPC response", e);
        }
    }

}
