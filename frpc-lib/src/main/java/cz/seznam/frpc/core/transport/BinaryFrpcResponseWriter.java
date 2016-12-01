package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.serialization.FrpcMarshaller;

import java.io.OutputStream;

/**
 * Implementation of {@link AbstractFrpcResponseWriter} capable of writing responses into binary {@code FRPC} format.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryFrpcResponseWriter extends AbstractFrpcResponseWriter {

    @Override
    protected void writeResponse(Object response, OutputStream outputStream) throws FrpcTransportException {
        writeInternal(response, outputStream);
    }

    @Override
    public void writeFault(FrpcFault fault, OutputStream outputStream) throws FrpcTransportException {
        writeInternal(fault, outputStream);
    }

    private void writeInternal(Object response, OutputStream outputStream) throws FrpcTransportException {
        FrpcMarshaller marshaller = new FrpcMarshaller(outputStream);
        marshaller.writeResponse(response);
    }

}
