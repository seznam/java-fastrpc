package cz.seznam.frpc;

import cz.seznam.frpc.handlers.*;
import cz.seznam.frpc.server.FrpcHandlerMapping;
import cz.seznam.frpc.server.FrpcServerUtils;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;

/**
 * Simple example demonstrating usage of FRPC framework with Jetty HTTP server.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcDemoServer {

    public static void main(String[] args) throws Exception {
        // the mapping ties bussines-logic classes (handlers) with FRPC method names
        FrpcHandlerMapping handlerMapping = new FrpcHandlerMapping();
        // FRPC handler can be a single instance of any custom class
        handlerMapping.addHandler("numberOperations", new Calculator());
        // or it can be specified by a class itself (the class has to have accessible no-arg constructor)
        handlerMapping.addHandler("stringOperations", StringOperations.class);
        handlerMapping.addHandler("arrayOperations", ArrayOperations.class);
        handlerMapping.addHandler("binaryOperations", BinaryOperations.class);
        // or Supplier can be used to provide instances of handler class
        handlerMapping.addHandler("collectionOperations", CollectionOperations.class, CollectionOperations::new);

        /* Jetty is the default HTTP server and we have jetty-specific FrpcRequestHandler implementation, but feel free
           to use something completely different. The main logic is abstracted by interfaces FrpcRequestProcessor
           and FrpcResultTransformer which fit together and can be easily plugged in to pretty much any HTTP server
           capable of returning request body as InputStream. */
        HttpConnectionFactory factory;

        Server server = new Server(9898);

        FrpcServerUtils.addDefaultFrpcHandler(server, "/RPC2", handlerMapping);
        server.start();
    }

}
