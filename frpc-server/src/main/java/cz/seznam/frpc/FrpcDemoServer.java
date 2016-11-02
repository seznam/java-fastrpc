package cz.seznam.frpc;

import cz.seznam.frpc.handlers.*;
import cz.seznam.frpc.server.FrpcHandlerMapping;
import cz.seznam.frpc.server.FrpcServerUtils;
import org.eclipse.jetty.server.Server;

public class FrpcDemoServer {

    public static void main(String[] args) throws Exception {
        FrpcHandlerMapping handlerMapping = new FrpcHandlerMapping();
        // FRPC handler can be a single instance of any custom class
        handlerMapping.addHandler("numberOperations", new Calculator());
        // or it can be specified by a class itself
        handlerMapping.addHandler("stringOperations", StringOperations.class);
        handlerMapping.addHandler("arrayOperations", ArrayOperations.class);
        handlerMapping.addHandler("binaryOperations", BinaryOperations.class);
        // or Supplier can be used to provide instances of handler class
        handlerMapping.addHandler("collectionOperations", CollectionOperations.class, CollectionOperations::new);


        Server server = new Server(9898);
        FrpcServerUtils.addDefaultFrpcHandler(server, "/RPC2", handlerMapping);
        server.start();
    }

}
