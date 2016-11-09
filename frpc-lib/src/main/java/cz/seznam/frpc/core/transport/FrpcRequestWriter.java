package cz.seznam.frpc.core.transport;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcRequestWriter extends FrpcWriter<FrpcRequest> {

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
