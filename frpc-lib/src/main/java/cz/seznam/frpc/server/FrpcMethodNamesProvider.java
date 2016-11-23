package cz.seznam.frpc.server;

import java.util.Set;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcMethodNamesProvider {

    public Set<String> listMethodNames();

}
