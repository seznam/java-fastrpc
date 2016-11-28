package cz.seznam.frpc.server;

/**
 * Object capable of handling (carrying out) {@code FRPC} method calls. Instances of this interface contain or delegate
 * to the actual business logic of {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcHandler {

    /**
     * Handles {@code FRPC} method call by either directly running the logic behind the method name or by delegating
     * the work elsewhere. Either way, the result is the actual result of the {@code FRPC} method.
     *
     * @param frpcMethodName name of the {@code FRPC} method to call
     * @param args array of arguments for the {@code FRPC} method with given name
     * @return actual result of the {@code FRPC} method
     * @throws Exception if anything goes wrong during computation
     */
    public Object handleFrpcMethodCall(String frpcMethodName, Object[] args) throws Exception;

}
