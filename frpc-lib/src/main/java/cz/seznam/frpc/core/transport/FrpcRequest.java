package cz.seznam.frpc.core.transport;

import java.util.*;

/**
 * Common abstraction of the request sent from {@code FRPC} client to {@code FRPC} server.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcRequest {

    private String methodName;
    private List<Object> parameters;

    /**
     * Creates new {@code FRPC} request representing an invocation of remote method with given name and parameters.
     *
     * @param methodName name of the remote method
     * @param parameters array of parameters to be passed to the remote method
     */
    public FrpcRequest(String methodName, Object[] parameters) {
        this.methodName = methodName;
        this.parameters = parameters == null ? Collections.emptyList() : Arrays.asList(parameters);
    }

    /**
     * Creates new {@code FRPC} request representing an invocation of remote method with given name and parameters.
     *
     * @param methodName name of the remote method
     * @param parameters list of parameters to be passed to the remote method
     */
    public FrpcRequest(String methodName, List<Object> parameters) {
        this.methodName = methodName;
        this.parameters = parameters == null ? Collections.emptyList() : new ArrayList<>(parameters);
    }

    /**
     * Returns name of the remote method to be called.
     *
     * @return name of the remote method to be called
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Returns all parameters (including implicit ones) to be passed to the remote method.
     *
     * @return all parameters (including implicit ones) to be passed to the remote method
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * Returns the same parameters as {@link #getParameters()} but in a form of array.
     *
     * @return the same parameters as {@link #getParameters()} but in a form of array
     */
    public Object[] getParametersAsArray() {
        return parameters.toArray();
    }

}
