package cz.seznam.frpc.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cz.seznam.frpc.FrpcBinUnmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcServer {

    // TODO: consider using another HTTP server implementation, this one provider only rather low level API
    private HttpServer httpServer;

    public FrpcServer(InetSocketAddress address, int socketBacklog, Set<String> requestMappings, FrpcHandlerMapping handlerMapping) {
        // require parameters are not null
        Objects.requireNonNull(address);
        Objects.requireNonNull(handlerMapping);
        Objects.requireNonNull(requestMappings);
        // try to initialize transport stuff
        try {
            // initialize http server
            HttpServer httpServer = HttpServer.create(address, socketBacklog);
            // create handler
            FrpcServerHttpHandler handler = new FrpcServerHttpHandler(handlerMapping.getMapping());
            // bind it to all request mappings
            requestMappings.forEach(m -> httpServer.createContext(m, handler));
            // start the server
            httpServer.start();
            // save it for later
            this.httpServer = httpServer;
        } catch (IOException e) {
            throw new RuntimeException("Error while creating HTTP server", e);
        }
    }

    private class FrpcServerHttpHandler implements HttpHandler {

        private static final String DEFAULT_HANDLER_NAME = "";

        private Map<String, Pair<MethodNameToParameterTypesMapper, FrpcHandler>> handlerMapping;

        public FrpcServerHttpHandler(Map<String, Pair<MethodNameToParameterTypesMapper, FrpcHandler>> handlerMapping) {
            this.handlerMapping = new HashMap<>(handlerMapping);
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            // get the request body
            InputStream is = httpExchange.getRequestBody();
            // create FRPC unmarshaller
            FrpcBinUnmarshaller unmarshaller = new FrpcBinUnmarshaller(is);

            String methodName;
            try {
                // try to unmarshall method name
                methodName = unmarshaller.unmarshallMethodName();
                // check if there is a dot somewhere in the method name
                int lastDotIndex = methodName.lastIndexOf('.');

                String handlerName;
                String handlerMethodName;
                // if there is no dot
                if(lastDotIndex == -1) {
                    // handler is not specified and the whole unmarshalled method name is the name of a method
                    handlerName = DEFAULT_HANDLER_NAME;
                    handlerMethodName = methodName;
                } else {
                    // otherwise everything up to the last dot is the handler name
                    handlerName = methodName.substring(0, lastDotIndex);
                    // and the rest is the actual method name
                    handlerMethodName = methodName.substring(lastDotIndex, methodName.length());
                }

                // try to find the handler first
                Pair<MethodNameToParameterTypesMapper, FrpcHandler> handlerPair = handlerMapping.get(handlerName);
                // check if we have any handler mapped to this name
                if(handlerPair == null) {
                    // TODO: handle error
                }
                // get the method mapper
                MethodNameToParameterTypesMapper mapper = handlerPair.getFirst();
                // try to map method name to its parameter types
                Class<?>[] parameterTypes = mapper.mapToParameterTypes(handlerMethodName);
                // TODO: unmarshall as many objects as there are parameter types and then check if their types comply with types in parameterTypes
                // TODO: if it's all right, call the handler with unmarshalled parameters
            } catch (Exception e) {
                // TODO: handle error
            }
        }

        private void handleError(HttpExchange exchange, Exception exception) {

        }

    }

}
