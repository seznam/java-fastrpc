package cz.seznam.frpc.core.transport;

import java.io.OutputStream;

/**
 * Specialization of {@link FrpcWriter} capable of writing any objects as method responses.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcResponseWriter extends FrpcWriter<Object> {

    public void write(Object response, OutputStream outputStream) throws FrpcTransportException;

    /**
     * Returns proper implementation of the writer for given protocol.
     *
     * @param protocol protocol to return writer for
     * @return proper implementation of the writer for given protocol
     * @throws IllegalArgumentException if given protocol is unknown
     */
    public static FrpcResponseWriter forProtocol(Protocol protocol) {
        switch (protocol) {
            case FRPC:
                return new BinaryFrpcResponseWriter();
            case XML_RPC:
                return new XmlFrpcResponseWriter();
            default:
                throw new IllegalArgumentException("Unexpected protocol given " + protocol.name() + ", don't know " +
                        "what which " + FrpcRequestReader.class.getSimpleName() + " instance to create.");
        }
    }

}
