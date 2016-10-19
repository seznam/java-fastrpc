package cz.seznam.frpc.server;

import java.lang.reflect.Method;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
interface MethodLocator {

    public Method locateMethod(String methodName) throws NoSuchMethodException;

}
