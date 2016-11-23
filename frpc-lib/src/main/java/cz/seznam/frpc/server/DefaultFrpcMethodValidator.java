package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class DefaultFrpcMethodValidator extends FrpcMethodValidator {

    @Override
    public FrpcMethodValidationResult doAdditionalValidation(FrpcMethodMetaData methodMetaData) {
        return null;
    }
}
