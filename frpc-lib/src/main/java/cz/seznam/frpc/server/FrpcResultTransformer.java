package cz.seznam.frpc.server;

import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcResultTransformer {

    public Map<String, Object> transformOkResponse(FrpcRequestProcessingResult frpcRequestProcessingResult);

    public Map<String, Object> transformErrorResponse(String errorMessage);

    public Map<String, Object> transformErrorResponse(Exception exception);

}
