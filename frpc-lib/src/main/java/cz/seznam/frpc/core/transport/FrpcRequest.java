package cz.seznam.frpc.core.transport;

import java.util.*;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcRequest {

    private String methodName;
    private List<Object> parameters;

    public FrpcRequest(String methodName, Object[] parameters) {
        this(methodName, null, parameters);
    }

    public FrpcRequest(String methodName, Object[] implicitParameters, Object[] parameters) {
        this(methodName, implicitParameters == null ? null : Arrays.asList(implicitParameters),
                parameters == null ? null : Arrays.asList(parameters));
    }

    public FrpcRequest(String methodName, List<Object> parameters) {
        this(methodName, null, parameters);
    }

    public FrpcRequest(String methodName, List<Object> implicitParameters, List<Object> parameters) {
        this.methodName = Objects.requireNonNull(methodName);
        if(implicitParameters == null || implicitParameters.isEmpty()) {
            this.parameters = parameters == null ? Collections.emptyList() : new ArrayList<>(parameters);
        } else {
            if(parameters == null || parameters.isEmpty()) {
                this.parameters = new ArrayList<>(implicitParameters);
            } else {
                List<Object> params = new ArrayList<>();
                params.addAll(implicitParameters);
                params.addAll(parameters);
                this.parameters = params;
            }
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public Object[] getParametersAsArray() {
        return parameters.toArray();
    }

}
