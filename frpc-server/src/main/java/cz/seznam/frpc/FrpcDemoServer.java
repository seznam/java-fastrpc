package cz.seznam.frpc;

import cz.seznam.frpc.handlers.ArrayOperations;
import cz.seznam.frpc.handlers.Calculator;
import cz.seznam.frpc.handlers.CollectionOperations;
import cz.seznam.frpc.handlers.StringOperations;
import cz.seznam.frpc.server.FrpcHandlerMapping;
import cz.seznam.frpc.server.FrpcServerUtils;
import org.eclipse.jetty.server.Server;

public class FrpcDemoServer {

    public static void main(String[] args) throws Exception {
        FrpcHandlerMapping handlerMapping = new FrpcHandlerMapping();
        handlerMapping.addHandler("numberOperations", new Calculator());
        handlerMapping.addHandler("stringOperations", StringOperations.class);
        handlerMapping.addHandler("arrayOperations", ArrayOperations.class);
        handlerMapping.addHandler("collectionOperations", CollectionOperations.class, CollectionOperations::new);

        Server server = new Server(9898);
        FrpcServerUtils.addDefaultFrpcHandler(server, "/RPC2", handlerMapping);
        server.start();
    }

}
