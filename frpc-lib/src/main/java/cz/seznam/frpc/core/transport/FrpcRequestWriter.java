package cz.seznam.frpc.core.transport;

/**
 * Specialization of {@link FrpcWriter} capable of writing {@link FrpcRequest}s.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcRequestWriter extends FrpcWriter<FrpcRequest> {

    /**
     * Returns proper implementation of the writer for given protocol.
     *
     * @param protocol protocol to return writer for
     * @return proper implementation of the writer for given protocol
     * @throws IllegalArgumentException if given protocol is unknown
     */
    public static FrpcRequestWriter forProtocol(Protocol protocol) {
        switch (protocol) {
            case FRPC:
                return new BinaryFrpcRequestWriter();
            case XML_RPC:
                return new XmlFrpcRequestWriter();
            default:
                throw new IllegalArgumentException("Unexpected protocol given " + protocol.name() + ", don't know " +
                        "what which " + FrpcRequestWriter.class.getSimpleName() + " instance to create.");
        }
    }

}
