package cz.seznam.frpc.server;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcServerUtils {

    public static void addDefaultFrpcHandler(Server server, String contextPath, FrpcHandlerMapping handlerMapping) {
        addDefaultFrpcHandler(server, contextPath, handlerMapping, true);
    }

    public static void addDefaultFrpcHandler(Server server, String contextPath, FrpcHandlerMapping handlerMapping, boolean allowNullPathsInfo) {
        // create default handler
        Handler handler = new FrpcRequestHandler(new HandlerUsingFrpcRequestProcesor(handlerMapping));
        // map the handler to given context path
        handler = createContextHandler(contextPath, handler, allowNullPathsInfo);
        // set it to the server
        server.setHandler(handler);
    }

    public static Handler createContextHandler(String contextPath, Handler handler) {
        return createContextHandler(contextPath, handler, true);
    }

    public static Handler createContextHandler(String contextPath, Handler handler, boolean allowNullPathsInfo) {
        if(StringUtils.isBlank(contextPath)) {
            return handler;
        }
        ContextHandler contextHandler = new ContextHandler(contextPath);
        contextHandler.setHandler(handler);
        contextHandler.setAllowNullPathInfo(allowNullPathsInfo);
        return contextHandler;
    }

}
