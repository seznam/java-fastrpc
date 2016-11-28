package cz.seznam.frpc.server;

import cz.seznam.frpc.core.FrpcResponseUtils;
import org.eclipse.jetty.http.HttpStatus;

import java.util.Map;

/**
 * Default implementation of {@code FrpcResultTransformer} which converts any {@code FRPC} call result into a
 * {@code Map} containing additional information like status code and status message.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class MapFrpcResultTransformer implements FrpcResultTransformer<Map<String, Object>, Map<String, Object>> {

    /**
     * Transforms given {@code FrpcRequestProcessingResult} into a {@code Map} by getting the object wrapped by
     * given {@code frpcRequestProcessingResult} and doing following steps:
     * <ul>
     *     <li>
     *         If the object is a {@code Map}, this method checks whether this map contains keys
     *         {@link FrpcResponseUtils#STATUS_KEY} and {@link FrpcResponseUtils#STATUS_MESSAGE_KEY}. If any of them is
     *         missing, then a mapping is added from that key to its default "OK" value
     *         ({@link FrpcResponseUtils#OK_STATUS_CODE} or {@link FrpcResponseUtils#DEFAULT_OK_STATUS_MESSAGE}
     *         respectively).
     *     </li>
     *     <li>
     *         If the object is not a map, new map is created using {@link FrpcResponseUtils#ok()} and the object is stored in
     *         this map under the key obtained by calling {@link FrpcRequestProcessingResult#getMethodMetaData()}.
     *     </li>
     * </ul>
     *
     * @param frpcRequestProcessingResult result of {@code FRPC} method call to be converted into unified return type;
     *                                    wraps an object of arbitrary type
     * @return a map containing success status code, status message and the result of the {@code FRPC} method call
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> transformOkResponse(FrpcRequestProcessingResult frpcRequestProcessingResult) {
        Map<String, Object> result;
        // getResult the actual method call result and the resultKey to map the result to
        Object methodResult = frpcRequestProcessingResult.getMethodResult();
        String methodResponseKey = (String) frpcRequestProcessingResult.getMethodMetaData().getAdditionalData()
                .get("result");
        // if the methodResponseKey is empty, it's the same as null - we treat it as not set
        if(methodResponseKey != null && methodResponseKey.isEmpty()) {
            methodResponseKey = null;
        }

        // check if it's a map
        if(methodResult instanceof Map) {
            // it might be the actual result to be returned depending on what's in the methodResultResponseKey
            if(methodResponseKey != null) {
                // we actually need to put this map under given resultKey in the result map
                Map<String, Object> okResponse = FrpcResponseUtils.ok();
                okResponse.put(methodResponseKey, methodResult);
                // the result is the wrapper object
                result = okResponse;
            } else {
                // the result is the map itself
                result = (Map<String, Object>) methodResult;
                // this map should not be mapped to any resultKey, we just need to make sure it contains status code and status message
                ensureContainsMandatoryValues(result);
            }
        } else {
            // if it's not a map, then methodResponseKey must be specified
            if(methodResponseKey == null) {
                return transformError("Error while processing FRPC request, handler method does not return Map nor does it specify resultKey to map the result under");
            }
            // create an OK response map
            result = FrpcResponseUtils.ok();
            // put method result into it under specified resultKey
            result.put(methodResponseKey, methodResult);
        }
        // return the result map
        return result;
    }

    /**
     * Transforms given error message into a map by simply calling {@link FrpcResponseUtils#error(String)} with given argument.
     *
     * @param errorMessage message to transform into desired result type
     * @return a map containing error status code and given status message
     */
    @Override
    public Map<String, Object> transformError(String errorMessage) {
        // create the response with given error message
        return FrpcResponseUtils.error(errorMessage);
    }

    /**
     * Transforms given error message into a map by simply calling {@link FrpcResponseUtils#error(Exception)} with
     * given argument.
     *
     * @param exception exception to transform into desired result type
     * @return a map containing error status code and status message obtained from given exception
     */
    @Override
    public Map<String, Object> transformError(Exception exception) {
        // create response by wrapping an exception
        return FrpcResponseUtils.error(exception);
    }

    protected void ensureContainsMandatoryValues(Map<String, Object> map) {
        map.putIfAbsent(FrpcResponseUtils.STATUS_KEY, HttpStatus.OK_200);
        map.putIfAbsent(FrpcResponseUtils.STATUS_MESSAGE_KEY, FrpcResponseUtils.DEFAULT_OK_STATUS_MESSAGE);
    }

}
