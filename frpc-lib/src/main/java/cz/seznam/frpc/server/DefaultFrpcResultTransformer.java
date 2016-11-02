package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcUtils;

import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class DefaultFrpcResultTransformer extends AbstractFrpcResultTransformer {

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> transformOkResponse(FrpcRequestProcessingResult frpcRequestProcessingResult) {
        Map<String, Object> result;
        // getResult the actual method call result and the key to map the result to
        Object methodResult = frpcRequestProcessingResult.getMethodResult();
        String methodResponseKey = frpcRequestProcessingResult.getMethodResponseKey();
        // if the methodResponseKey is empty, it's the same as null - we treat it as not set
        if(methodResponseKey != null && methodResponseKey.isEmpty()) {
            methodResponseKey = null;
        }

        // check if it's a map
        if(methodResult instanceof Map) {
            // it might be the actual result to be returned depending on what's in the methodResultResponseKey
            if(methodResponseKey != null) {
                // we actually need to put this map under given key in the result map
                Map<String, Object> okResponse = FrpcUtils.ok();
                okResponse.put(methodResponseKey, methodResult);
                // the result is the wrapper object
                result = okResponse;
            } else {
                // the result is the map itself
                result = (Map<String, Object>) methodResult;
                // this map should not be mapped to any key, we just need to make sure it contains status code and status message
                ensureContainsMandatoryValues(result);
            }
        } else {
            // if it's not a map, then methodResponseKey must be specified
            if(methodResponseKey == null) {
                return transformErrorResponse("Error while processing FRPC request, handler method does not return Map nor does it specify key to map the result under");
            }
            // create an OK response map
            result = FrpcUtils.ok();
            // put method result into it under specified key
            result.put(methodResponseKey, methodResult);
        }
        // return the result map
        return result;
    }

    @Override
    public Map<String, Object> transformErrorResponse(String errorMessage) {
        // create the response with given error message
        return FrpcUtils.error(errorMessage);
    }

    @Override
    public Map<String, Object> transformErrorResponse(Exception exception) {
        // create response by wrapping an exception
        return FrpcUtils.wrapException(exception);
    }

}
