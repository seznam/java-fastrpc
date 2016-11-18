package cz.seznam.frpc.server;

/**
 * An object which can transform result of {@code FRPC} method invocation into desired type. The idea is to use
 * {@code FrpcResultTransformer} to transform any result returned in response to a {@code FRPC} call to one specific
 * type so that the API composed of different {@code FRPC} methods behaves consistently.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcResultTransformer<R, F> {

    /**
     * Transforms given {@link FrpcRequestProcessingResult} into a unified result type.
     *
     * @param frpcRequestProcessingResult result of {@code FRPC} method call to be converted into unified return type;
     *                                    wraps an object of arbitrary type
     * @return result of desired type constructed from given argument
     */
    public R transformOkResponse(FrpcRequestProcessingResult frpcRequestProcessingResult);

    /**
     * Transforms given error message into unified fault type.
     *
     * @param errorMessage message to transform into desired result type
     * @return result of desired type constructed from given argument
     */
    public F transformError(String errorMessage);

    /**
     * Transforms given exception into unified fault type.
     *
     * @param exception exception to transform into desired result type
     * @return result of desired type constructed from given argument
     */
    public F transformError(Exception exception);

}
