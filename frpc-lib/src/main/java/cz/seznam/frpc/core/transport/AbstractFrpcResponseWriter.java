package cz.seznam.frpc.core.transport;

import java.io.OutputStream;

/**
 * Abstract base class for {@link FrpcResponseWriter} implementations taking care of detecting {@link FrpcFault}s and
 * writing them separately from other types.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class AbstractFrpcResponseWriter implements FrpcResponseWriter {

    @Override
    public void write(Object response, OutputStream outputStream) throws FrpcTransportException {
        if(response instanceof FrpcFault) {
            writeFault(((FrpcFault) response), outputStream);
        } else {
            writeResponse(response, outputStream);
        }
    }

    protected abstract void writeResponse(Object response, OutputStream outputStream) throws FrpcTransportException;

    protected abstract void writeFault(FrpcFault fault, OutputStream outputStream) throws FrpcTransportException;

}
