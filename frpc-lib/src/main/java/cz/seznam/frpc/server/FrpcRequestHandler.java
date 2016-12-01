package cz.seznam.frpc.server;

import cz.seznam.frpc.core.FrpcDataProcessingException;
import cz.seznam.frpc.core.transport.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * Default <strong>request</strong> handler for {@code FRPC} requests designed to work with Jetty HTTP server.
 * Do not mistake with {@link FrpcHandler} which handles the actual method call. This class handles HTTP requests
 * received by the HTTP server and delegates them to {@link FrpcRequestProcessor}.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcRequestHandler extends AbstractHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcRequestHandler.class);

    private FrpcRequestProcessor frpcRequestProcessor;
    private FrpcResultTransformer<?, ?> frpcResultTransformer;

    /**
     * Creates new instance with given {@code FrpcRequestProcessor} used to process requests and
     * {@link DefaultFrpcResultTransformer} used to transform its results.
     *
     * @param frpcRequestProcessor request processor to be used to process incoming requests
     */
    public FrpcRequestHandler(FrpcRequestProcessor frpcRequestProcessor) {
        this(frpcRequestProcessor, new DefaultFrpcResultTransformer());
    }

    /**
     * Creates new instance with given arguments.
     *
     * @param frpcRequestProcessor request processor to be used to process incoming requests
     * @param frpcResultTransformer result transformer used to transform results
     */
    public FrpcRequestHandler(FrpcRequestProcessor frpcRequestProcessor,
                              FrpcResultTransformer<?, ?> frpcResultTransformer) {
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
            Object result;
            // try to handle the request
            Object handlerResult;
            // default protocol is XML-RPC
            Protocol protocol = Protocol.XML_RPC;
            try {
                // get content type
                String contentType = request.getContentType();
                if(contentType == null) {
                    contentType = Protocol.XML_RPC.getContentType();
                    LOGGER.debug("Content type header is missing, defaulting to {}", contentType);
                }
                // try to get Protocol from content type
                try {
                    protocol = Protocol.fromContentType(contentType);
                } catch (IllegalArgumentException e) {
                    throw new FrpcTransportException("Given content type is not supported by any known protocol", e);
                }
                // check that content length is specified
                if(protocol == Protocol.FRPC && request.getContentLengthLong() < 0) {
                    throw new FrpcTransportException("Content length must be specified");
                }

                handlerResult = doHandle(request, protocol);
            } catch (Exception e) {
                LOGGER.debug("Caught exception from method {}", request.getMethod(), e);
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
                handleResponse(result, response, protocol);
            } catch (Exception e) {
                // if we can't properly handle the response, just return 500 with no content
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
        } else {
            // if the HTTP method is not POST, return 405
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            // and set the request as handled so that the server know we actually did something (otherwise it would
            // return 404 even though we specified 405)
            baseRequest.setHandled(true);
        }
        // add response headers
        addResponseHeaders(response);
    }

    private FrpcRequestProcessingResult doHandle(HttpServletRequest request, Protocol protocol) throws Exception {
        // get request reader for protocol
        FrpcRequestReader requestReader = FrpcRequestReader.forProtocol(protocol);
        // read the request
        FrpcRequest frpcRequest = requestReader.read(request.getInputStream(), request.getContentLength());
        // process it using the request processor
        return frpcRequestProcessor.process(frpcRequest);
    }

    private void handleResponse(Object result, HttpServletResponse response, Protocol protocol) throws FrpcDataProcessingException,
            IOException {
        // create response writer for given protocol
        FrpcResponseWriter responseWriter = FrpcResponseWriter.forProtocol(protocol);
        // write response to byte array so that we can set content length header properly
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // write result to the response
        responseWriter.write(result, baos);
        // set response properties
        response.setStatus(HttpStatus.OK_200);
        response.setContentType(protocol.getContentType());
        response.setContentLength(baos.size());
        // write response body
        response.getOutputStream().write(baos.toByteArray());
    }

    private void addResponseHeaders(HttpServletResponse response) {
        response.addHeader(HttpHeader.ACCEPT.asString(), "text/xml, application/x-frpc");
    }

}
