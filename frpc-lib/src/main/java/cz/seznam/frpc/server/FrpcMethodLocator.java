package cz.seznam.frpc.server;

import java.lang.reflect.Method;

/**
 * An object which is capable of returning actual Java {@link Method} based on given {@code FRPC} method name.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
interface FrpcMethodLocator {

    /**
     * Returns an actual Java {@link Method} based on given {@code FRPC} method name.
     *
     * @param frpcMethodName {@code FRPC} method name to locate a method by
     * @return an actual Java method corresponding to given {@code FRPC} method name
     * @throws NoSuchMethodException if there is no method for given name
     */
    public Method locateMethodByFrpcName(String frpcMethodName) throws NoSuchMethodException;

}
