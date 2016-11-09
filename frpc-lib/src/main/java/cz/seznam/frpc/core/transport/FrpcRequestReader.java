package cz.seznam.frpc.core.transport;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcRequestReader extends FrpcReader<FrpcRequest> {

    public static FrpcRequestReader forProtocol(Protocol protocol) {
        switch (protocol) {
            case FRPC:
                return new BinaryFrpcRequestReader();
            case XML_RPC:
                return new XmlFrpcRequestReader();
            default:
                throw new IllegalArgumentException("Unexpected protocol given " + protocol.name() + ", don't know " +
                        "what which " + FrpcRequestReader.class.getSimpleName() + " instance to create.");
        }
    }

}
