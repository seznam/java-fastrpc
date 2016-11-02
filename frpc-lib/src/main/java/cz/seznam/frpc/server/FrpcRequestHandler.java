package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcMarshaller;
import cz.seznam.frpc.FrpcDataException;
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

    private FrpcRequestProcessor frpcRequestProcessor;
    private FrpcResultTransformer frpcResultTransformer;

    public FrpcRequestHandler(FrpcRequestProcessor frpcRequestProcessor) {
        this.frpcRequestProcessor = Objects.requireNonNull(frpcRequestProcessor);
        this.frpcResultTransformer = new DefaultFrpcResultTransformer();
    }

    public FrpcRequestHandler(FrpcRequestProcessor frpcRequestProcessor, FrpcResultTransformer frpcResultTransformer) {
        this.frpcRequestProcessor = Objects.requireNonNull(frpcRequestProcessor);
        this.frpcResultTransformer = Objects.requireNonNull(frpcResultTransformer);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Map<String, Object> result;
        // try to handle the request
        Object handlerResult;
        try {
            handlerResult = doHandle(target, baseRequest, request, response);
        } catch (Exception e) {
            handlerResult = e;
        }

        try {
            // if the result is an exception
            if(handlerResult instanceof Exception) {
                // transform the exception into result map
                result = frpcResultTransformer.transformErrorResponse((Exception) handlerResult);
            } else {
                // otherwise transform the result into result map
                result = frpcResultTransformer.transformOkResponse((FrpcRequestProcessingResult) handlerResult);
            }
            // serialize the result into the response
            handleResponse(result, response);
        } catch (Exception e) {
            // if we can't properly handle the response, just return 500 with no content
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    private FrpcRequestProcessingResult doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // getResult the request body
        InputStream is = request.getInputStream();
        // process it using the request processor
        return frpcRequestProcessor.process(is);
    }

    private void handleResponse(Object result, HttpServletResponse response) throws FrpcDataException, IOException {
        // marshall response
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FrpcMarshaller marshaller = new FrpcMarshaller(baos);
        marshaller.packMagic();
        marshaller.packItem(result);
        // set it to the response
        byte[] bytes = baos.toByteArray();
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.setStatus(HttpStatus.OK_200);
    }

}
