package cz.seznam.frpc.server;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for {@code FRPC} server.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcServerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcServerUtils.class);

    private static final Object CANNOT_CONVERT = new Object();

    /**
     * Convenience method for calling {@link #addDefaultFrpcHandler(Server, String, FrpcHandlerMapping, boolean)} with
     * {@code true} as the last argument.
     *
     * @param server server to add the default request handler to
     * @param contextPath context path to map the handler to
     * @param handlerMapping handler mapping used to initialize an instance of {@code HandlerUsingFrpcRequestProcesor}
     *                       used by the default request handler
     */
    public static void addDefaultFrpcHandler(Server server, String contextPath, FrpcHandlerMapping handlerMapping) {
        addDefaultFrpcHandler(server, contextPath, handlerMapping, true);
    }

    /**
     * Adds default {@code FRPC} handler (which is {@link FrpcRequestHandler}) to given {@code Server}. The
     * {@code FrpcRequestHandler} is initialized with new instance of {@link HandlerUsingFrpcRequestProcesor} using
     * given {@code handlerMapping}. <br />
     * Newly created {@code FrpcRequestHandler} is then wrapped by context handler using
     * {@link #createContextHandler(String, Handler, boolean)} before being set to the server.
     *
     * @param server server to add the default request handler to
     * @param contextPath context path to map the handler to
     * @param handlerMapping handler mapping used to initialize an instance of {@code HandlerUsingFrpcRequestProcesor}
     *                       used by the default request handler
     * @param allowNullPathsInfo if {@code true} then no redirect is done by the server in case it encounters an
     *                           absolute URL matching given {@code contextPath} and not ending with a trailing slash;
     *                           if {@code false} then redirect to the URL with added trailing slash is done by the
     *                           server
     */
    public static void addDefaultFrpcHandler(Server server, String contextPath, FrpcHandlerMapping handlerMapping,
                                             boolean allowNullPathsInfo) {
        // create default handler
        Handler handler = new FrpcRequestHandler(new HandlerUsingFrpcRequestProcesor(handlerMapping));
        // map the handler to given context path
        handler = createContextHandler(contextPath, handler, allowNullPathsInfo);
        // set it to the server
        server.setHandler(handler);
    }

    /**
     * Convenience method for calling {@link #createContextHandler(String, Handler, boolean)} with {@code true} as the
     * last argument.
     *
     * @param contextPath context path to map the handler to
     * @param handler handler to delegate to through the newly created {@code ContextHandler}
     * @return either new instance of {@link ContextHandler} wrapping given {@code handler} and mapped to given
     *         {@code contextPath} or given {@code handler} unmodified (if the {@code contextPath} is blank)
     */
    public static Handler createContextHandler(String contextPath, Handler handler) {
        return createContextHandler(contextPath, handler, true);
    }

    /**
     * Creates an instance of {@link ContextHandler} from given {@code contextPath}. Given {@code handler} is set as the
     * underlying handler for the newly created {@code ContextHandler} instance. <br />
     * If given {@code contextPath} is {@code null} or empty, then given {@code handler} is returned as is.
     *
     * @param contextPath context path to map the handler to
     * @param handler handler to delegate to through the newly created {@code ContextHandler}
     * @param allowNullPathsInfo if {@code true} then no redirect is done by the server in case it encounters an
     *                           absolute URL matching given {@code contextPath} and not ending with a trailing slash;
     *                           if {@code false} then redirect to the URL with added trailing slash is done by the
     *                           server
     * @return either new instance of {@link ContextHandler} wrapping given {@code handler} and mapped to given
     *         {@code contextPath} or given {@code handler} unmodified (if the {@code contextPath} is blank)
     */
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
