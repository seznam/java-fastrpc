package cz.seznam.frpc.client;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface UnwrappableFrpcCallResult {

    public UnwrappedFrpcCallResult unwrap();

}
