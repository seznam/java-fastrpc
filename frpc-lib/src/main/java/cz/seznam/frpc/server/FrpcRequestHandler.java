package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcBinMarshaller;
import cz.seznam.frpc.FrpcDataException;
import cz.seznam.frpc.FrpcUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcRequestHandler extends AbstractHandler {

    private FrpcRequestProcessor requestProcessor;

    public FrpcRequestHandler(FrpcRequestProcessor frpcRequestProcessor) {
        this.requestProcessor = Objects.requireNonNull(frpcRequestProcessor);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // try to handle the request
        Object result;
        try {
            result = doHandle(target, baseRequest, request, response);
        } catch (Exception e) {
            result = e;
        }

        try {
            // if the result is an exception
            if(result instanceof Exception) {
                // serialize the exception into the response
                handleErrorResponse((Exception) result, response);
            } else {
                // otherwise serialize the result into the response
                handleOkResponse((FrpcRequestProcessingResult) result, response);
            }
        } catch (Exception e) {
            // if we can't properly handle the response, just return 500 with no content
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    private FrpcRequestProcessingResult doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // get the request body
        InputStream is = request.getInputStream();
        // process it using the request processor
        return requestProcessor.process(is);
    }

    @SuppressWarnings("unchecked")
    private void handleOkResponse(FrpcRequestProcessingResult frpcRequestProcessingResult, HttpServletResponse response) throws FrpcDataException, IOException {
        Object result;
        // get the actual method call result and the key to map the result to
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
                // this map should not be mapped to any key, we just need to make sure it contains status code and status message
                ensureContainsMandatoryValues((Map<String, Object>) methodResult);
                // the result is the map itself
                result = methodResult;
            }
        } else {
            // if it's not a map, then methodResponseKey must be specified
            if(methodResponseKey == null) {
                handleErrorResponse("Error while processing FRPC request, handler method does not return Map not does it specify key to map the result under", response);
                return;
            }
            // create an OK response map
            Map<String, Object> okResponse = FrpcUtils.ok();
            // put method result into it under specified key
            okResponse.put(methodResponseKey, methodResult);
            // the result is this map
            result = okResponse;
        }
        // serialize the result object into the response
        handleResponse(result, response, HttpStatus.OK_200);
    }

    private void handleErrorResponse(String errorMessage, HttpServletResponse response) throws FrpcDataException, IOException {
        // create the response with given error message
        Map<String, Object> result = FrpcUtils.error(errorMessage);
        // handle it as an error
        handleResponse(result, response, HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    private void handleErrorResponse(Exception exception, HttpServletResponse response) throws FrpcDataException, IOException {
        // create response by wrapping an exception
        Map<String, Object> result = FrpcUtils.wrapException(exception);
        // handle the response as error
        handleResponse(result, response, HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    private void handleResponse(Object result, HttpServletResponse response, int responseStatus) throws FrpcDataException, IOException {
        // marshall response
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FrpcBinMarshaller marshaller = new FrpcBinMarshaller(baos);
        marshaller.packMagic();
        marshaller.packItem(result);
        // set it to the response
        byte[] bytes = baos.toByteArray();
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.setStatus(responseStatus);
    }

    private void ensureContainsMandatoryValues(Map<String, Object> map) {
        map.putIfAbsent(FrpcUtils.STATUS_KEY, HttpStatus.OK_200);
        map.putIfAbsent(FrpcUtils.STATUS_MESSAGE_KEY, FrpcUtils.DEFAULT_OK_STATUS_MESSAGE);
    }

}
