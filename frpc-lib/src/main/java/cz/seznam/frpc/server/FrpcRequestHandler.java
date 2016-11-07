package cz.seznam.frpc.server;

import cz.seznam.frpc.common.FrpcMarshaller;
import cz.seznam.frpc.common.FrpcDataException;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
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
 * Default <strong>request</strong> handler for {@code FRPC} requests designed to work with Jetty HTTP server.
 * Do not mistake with {@link FrpcHandler} which handles the actual method call. This class handles HTTP requests
 * received by the HTTP server and delegates them to {@link FrpcRequestProcessor}.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcRequestHandler extends AbstractHandler {

    private FrpcRequestProcessor frpcRequestProcessor;
    private FrpcResultTransformer<Map<String, Object>> frpcResultTransformer;

    /**
     * Creates new instance with given {@code FrpcRequestProcessor} used to process requests and
     * {@link DefaultFrpcResultTransformer} used to transform its results.
     *
     * @param frpcRequestProcessor request processor to be used to process incoming requests
     */
    public FrpcRequestHandler(FrpcRequestProcessor frpcRequestProcessor) {
        this.frpcRequestProcessor = Objects.requireNonNull(frpcRequestProcessor);
        this.frpcResultTransformer = new DefaultFrpcResultTransformer();
    }

    /**
     * Creates new instance with given arguments.
     *
     * @param frpcRequestProcessor request processor to be used to process incoming requests
     * @param frpcResultTransformer result transformer used to transform results
     */
    public FrpcRequestHandler(FrpcRequestProcessor frpcRequestProcessor,
                              FrpcResultTransformer<Map<String, Object>> frpcResultTransformer) {
        this.frpcRequestProcessor = Objects.requireNonNull(frpcRequestProcessor);
        this.frpcResultTransformer = Objects.requireNonNull(frpcResultTransformer);
    }

    /**
     * Handles Jetty's HTTP request. Internally only takes the request body and delegates its processing to specified
     * {@link FrpcRequestProcessor}. The result (which might be either a value returned by the request processor or any
     * exception it throws) is then handled by specified {@link FrpcResultTransformer}.
     */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // only handle POST request as FRPC method calls
        if(HttpMethod.POST.is(request.getMethod())) {
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
                    result = frpcResultTransformer.transformError((Exception) handlerResult);
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
        } else {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            baseRequest.setHandled(true);
        }
        // add response headers
        addResponseHeaders(response);
    }

    private FrpcRequestProcessingResult doHandle(String target, Request baseRequest, HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
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

    private void addResponseHeaders(HttpServletResponse response) {
        response.addHeader(HttpHeader.ACCEPT.asString(), "text/xml, application/x-frpc");
    }

}
