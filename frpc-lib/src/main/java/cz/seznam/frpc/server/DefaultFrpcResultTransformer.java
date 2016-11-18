package cz.seznam.frpc.server;

import cz.seznam.frpc.core.transport.FrpcFault;
import org.eclipse.jetty.http.HttpStatus;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class DefaultFrpcResultTransformer implements FrpcResultTransformer<Object, FrpcFault> {

    @Override
    public Object transformOkResponse(FrpcRequestProcessingResult frpcRequestProcessingResult) {
        return frpcRequestProcessingResult.getMethodResult();
    }

    @Override
    public FrpcFault transformError(String errorMessage) {
        return new FrpcFault(HttpStatus.INTERNAL_SERVER_ERROR_500, errorMessage);
    }

    @Override
    public FrpcFault transformError(Exception exception) {
        return new FrpcFault(HttpStatus.INTERNAL_SERVER_ERROR_500, exception.getMessage());
    }
}
