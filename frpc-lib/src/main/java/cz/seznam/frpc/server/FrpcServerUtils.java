package cz.seznam.frpc.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcServerUtils {

    public static Handler createContextHandler(String contextPath, Handler handler) {
        if(contextPath == null || contextPath.isEmpty()) {
            return handler;
        }
        ContextHandler contextHandler = new ContextHandler(contextPath);
        contextHandler.setHandler(handler);
        return contextHandler;
    }

}
