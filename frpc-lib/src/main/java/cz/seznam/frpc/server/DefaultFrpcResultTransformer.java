package cz.seznam.frpc.server;

import cz.seznam.frpc.core.transport.FrpcFault;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Implementation of {@link FrpcResultTransformer} transforming successful responses into {@code Object}s and
 * errors into {@link FrpcFault}s.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class DefaultFrpcResultTransformer implements FrpcResultTransformer<Object, FrpcFault> {

    /**
     * Returns the object wrapped by given {@link FrpcRequestProcessingResult}.
     *
     * @param frpcRequestProcessingResult result of {@code FRPC} method call to be converted into unified return type;
     *                                    wraps an object of arbitrary type
     * @return the object wrapped by given {@link FrpcRequestProcessingResult}
     */
    @Override
    public Object transformOkResponse(FrpcRequestProcessingResult frpcRequestProcessingResult) {
        return frpcRequestProcessingResult.getMethodResult();
    }

    /**
     * Constructs new {@link FrpcFault} with status code {@code 500} and given status message.
     *
     * @param errorMessage message to transform into desired result type
     * @return new {@code FrpcFault} as described above
     */
    @Override
    public FrpcFault transformError(String errorMessage) {
        return new FrpcFault(HttpStatus.INTERNAL_SERVER_ERROR_500, errorMessage);
    }

    /**
     * Constructs new {@link FrpcFault} with status code {@code 500} and status message obtained by calling
     * {@link Exception#getMessage()} on given exception.
     *
     * @param exception exception to transform into desired result type
     * @return new {@code FrpcFault} as described above
     */
    @Override
    public FrpcFault transformError(Exception exception) {
        return new FrpcFault(HttpStatus.INTERNAL_SERVER_ERROR_500, exception.getMessage());
    }
}
