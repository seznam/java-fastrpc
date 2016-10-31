package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcUtils;
import org.eclipse.jetty.http.HttpStatus;

import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class AbstractFrpcResultTransformer implements FrpcResultTransformer {

    public abstract Map<String, Object> transformOkResponse(FrpcRequestProcessingResult frpcRequestProcessingResult);

    public abstract Map<String, Object> transformErrorResponse(String errorMessage);

    public abstract Map<String, Object> transformErrorResponse(Exception exception);

    protected void ensureContainsMandatoryValues(Map<String, Object> map) {
        map.putIfAbsent(FrpcUtils.STATUS_KEY, HttpStatus.OK_200);
        map.putIfAbsent(FrpcUtils.STATUS_MESSAGE_KEY, FrpcUtils.DEFAULT_OK_STATUS_MESSAGE);
    }

}
