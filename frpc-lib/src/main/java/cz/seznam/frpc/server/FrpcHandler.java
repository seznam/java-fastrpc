package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcHandler {

    Object handleRequest(String methodName, Object[] args) throws RequestProcessingException;

}
