package cz.seznam.frpc.server;

import java.lang.reflect.Method;

/**
 * An object which is capable of returning actual Java {@link Method} based on given {@code FRPC} method value.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
interface FrpcHandlerMethodLocator {

    /**
     * Returns an actual Java {@link Method} based on given {@code FRPC} method value.
     *
     * @param frpcMethodName {@code FRPC} method value to locate a method by
     * @return an actual Java method corresponding to given {@code FRPC} method value
     * @throws NoSuchMethodException if there is no method for given value
     */
    public Method locateMethodByFrpcName(String frpcMethodName) throws NoSuchMethodException;

}
