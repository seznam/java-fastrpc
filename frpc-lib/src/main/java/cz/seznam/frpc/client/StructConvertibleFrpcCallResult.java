package cz.seznam.frpc.client;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface StructConvertibleFrpcCallResult<S extends AbstractFrpcCallResult> {

    public S asStruct();

}
