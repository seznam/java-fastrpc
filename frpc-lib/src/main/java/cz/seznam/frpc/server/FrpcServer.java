package cz.seznam.frpc.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcServer {

    private Server server;

    public FrpcServer(int port, FrpcHandlerMapping handlerMapping) {
        this(new Server(port), null, handlerMapping);
    }

    public FrpcServer(int port, String context, FrpcHandlerMapping handlerMapping) {
        this(new Server(port), context, handlerMapping);
    }

    public FrpcServer(InetSocketAddress address, FrpcHandlerMapping handlerMapping) {
        this(new Server(Objects.requireNonNull(address)), null, handlerMapping);
    }

    public FrpcServer(InetSocketAddress address, String context, FrpcHandlerMapping handlerMapping) {
        this(new Server(Objects.requireNonNull(address)), context, handlerMapping);
    }

    public FrpcServer(Server server, FrpcHandlerMapping handlerMapping) {
        this(server, null, handlerMapping);
    }

    public FrpcServer(Server server, String contextPath, FrpcHandlerMapping handlerMapping) {
        this.server = Objects.requireNonNull(server);
        // create Frpc handler either way
        Handler frpcHandler = new FrpcRequestHandler(new HandlerUsingFrpcRequestProcesor(handlerMapping));
        // if context path is given, wrap it into ContextHandler
        frpcHandler = FrpcServerUtils.createContextHandler(contextPath, frpcHandler);
        // and set this handler to the server
        server.setHandler(frpcHandler);
        // start the server
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("The server failed to start", e);
        }
    }

}
