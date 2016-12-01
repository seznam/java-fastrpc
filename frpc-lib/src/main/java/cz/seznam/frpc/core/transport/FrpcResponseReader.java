package cz.seznam.frpc.core.transport;

/**
 * Specialization of {@link FrpcReader} capable of reading {@code FRPC} responses as objects.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcResponseReader extends FrpcReader<Object> {

    /**
     * Returns proper implementation of the reader for given protocol.
     *
     * @param protocol protocol to return reader for
     * @return proper implementation of the reader for given protocol
     * @throws IllegalArgumentException if given protocol is unknown
     */
    public static FrpcResponseReader forProtocol(Protocol protocol) {
        switch (protocol) {
            case FRPC:
                return new BinaryFrpcResponseReader();
            case XML_RPC:
                return new XmlFrpcResponseReader();
            default:
                throw new IllegalArgumentException("Unexpected protocol given " + protocol.name() + ", don't know " +
                        "what which " + FrpcRequestReader.class.getSimpleName() + " instance to create.");
        }
    }

}
