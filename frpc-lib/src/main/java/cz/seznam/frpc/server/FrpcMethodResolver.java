package cz.seznam.frpc.server;

import java.util.Map;

/**
 * An object capable of locating and describing {@code FRPC} methods provided by given class.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcMethodResolver<T> {

    /**
     * Examines given class and locates all {@code FRPC} methods it provides. The result is a mapping from names of
     * these methods <strong>within the handler</strong> (that means no dots are allowed) to instances of
     * {@link FrpcMethodMetaData} describe these methods.
     *
     * @param examined class to be examined
     * @return a map mapping {@code FRPC} method names to their descriptions in form of {@code FrpcMethodMetaData}
     */
    public Map<String, FrpcMethodMetaData> resolveFrpcMethods(Class<T> examined);

}
