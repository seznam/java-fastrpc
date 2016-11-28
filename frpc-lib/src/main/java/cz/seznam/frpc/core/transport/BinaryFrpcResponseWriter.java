package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.serialization.FrpcMarshaller;

import java.io.OutputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryFrpcResponseWriter implements FrpcResponseWriter {

    @Override
    public void write(Object response, OutputStream outputStream) throws FrpcTransportException {
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
