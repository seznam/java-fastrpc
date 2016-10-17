package cz.seznam.frpc.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcServer {

    private HttpServer httpServer;

    public FrpcServer(InetSocketAddress address, int socketBacklog, Set<String> requestMappings, Map<String, FrpcHandler> handlerMapping) {
        // require parameters are not null
        Objects.requireNonNull(address);
        Objects.requireNonNull(handlerMapping);
        Objects.requireNonNull(requestMappings);
        // try to initialize transport stuff
        try {
            // initialize http server
            HttpServer httpServer = HttpServer.create(address, socketBacklog);
            // create handler
            FrpcServerHttpHandler handler = new FrpcServerHttpHandler(handlerMapping);
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

        private Map<String, FrpcHandler> handlerMapping;

        public FrpcServerHttpHandler(Map<String, FrpcHandler> handlerMapping) {
            this.handlerMapping = new HashMap<>(handlerMapping);
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            // TODO: implement this
        }

    }

}
