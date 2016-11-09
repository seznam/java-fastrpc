package cz.seznam.frpc.core.transport;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcResponseReader extends FrpcReader<Object> {

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
