package cz.seznam.frpc.core.transport;

import java.util.*;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcRequest {

    private String methodName;
    private List<Object> parameters;

    public FrpcRequest(String methodName, Object[] parameters) {
        this(methodName, null, false, parameters);
    }

    public FrpcRequest(String methodName, Object[] implicitParameters, boolean prependImplicitParams,
                       Object[] parameters) {
        this(methodName, implicitParameters == null ? null : Arrays.asList(implicitParameters), prependImplicitParams,
                parameters == null ? null : Arrays.asList(parameters));
    }

    public FrpcRequest(String methodName, List<Object> parameters) {
        this(methodName, null, false, parameters);
    }

    public FrpcRequest(String methodName, List<Object> implicitParameters, boolean prependImplicitParams,
                       List<Object> parameters) {
        this.methodName = Objects.requireNonNull(methodName);
        if (implicitParameters == null || implicitParameters.isEmpty()) {
            this.parameters = parameters == null ? Collections.emptyList() : new ArrayList<>(parameters);
        } else {
            if (parameters == null || parameters.isEmpty()) {
                this.parameters = new ArrayList<>(implicitParameters);
            } else {
                List<Object> params = new ArrayList<>();
                if (prependImplicitParams) {
                    params.addAll(implicitParameters);
                }
                params.addAll(parameters);
                if (!prependImplicitParams) {
                    params.addAll(implicitParameters);
                }
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
