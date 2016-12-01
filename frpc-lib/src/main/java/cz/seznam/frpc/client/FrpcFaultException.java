package cz.seznam.frpc.client;

import cz.seznam.frpc.core.transport.FrpcFault;

/**
 * Thrown to indicate when trying to convert result of {@code FRPC} method call which is a {@link FrpcFault} into any
 * other type. The fault can be retrieved from the exception if it gets caught.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcFaultException extends RuntimeException {

    private FrpcFault fault;

    /**
     * Creates new instance wrapping given {@code FrpcFault}.
     *
     * @param fault {@code FrpcFault} to wrap so taht it can be retrieved later
     */
    public FrpcFaultException(FrpcFault fault) {
        this.fault = fault;
    }

    @Override
    public String getMessage() {
        return "FRPC fault: " + (fault == null ? "null" : fault.toString());
    }

    /**
     * Returns the {@link FrpcFault} instance wrapped by this exception.
     *
     * @return the {@link FrpcFault} instance wrapped by this exception
     */
    public FrpcFault getFault() {
        return fault;
    }

}
