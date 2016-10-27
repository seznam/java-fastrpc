package cz.seznam.frpc;

import cz.seznam.frpc.handlers.Calculator;
import cz.seznam.frpc.server.FrpcHandlerMapping;
import cz.seznam.frpc.server.FrpcServer;

import java.io.IOException;

public class FrpcDemoServer {

    public static void main(String[] args) throws IOException {
        FrpcHandlerMapping handlerMapping = new FrpcHandlerMapping();
        handlerMapping.addHandler("operations", Calculator.class);

        FrpcServer server = new FrpcServer(9898, "/RPC2/", handlerMapping);
    }

}
