package cz.seznam.frpc.server;

import java.util.Set;

/**
 * An object capable of listing names of {@code FRPC} methods provided by {@code FrpcHandler}.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcMethodNamesProvider {

    /**
     * Returns names of all methods provided by given {@link FrpcHandler} in a set. Names of these methods must be
     * <strong>relative</strong> for given handler, i.e. no <strong>full</strong> names are expected. That means
     * returned method names must not contain any dots.
     *
     * @param handler handler to list method names for
     * @return names of all methods provided by given {@code FrpcHandler} in a set
     */
    public Set<String> listMethodNames(FrpcHandler handler);

}
