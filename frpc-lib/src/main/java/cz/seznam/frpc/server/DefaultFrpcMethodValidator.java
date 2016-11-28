package cz.seznam.frpc.server;

/**
 * Implementation of {@link FrpcMethodValidator} doing no additional validation.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class DefaultFrpcMethodValidator extends FrpcMethodValidator {

    /**
     * Does nothing, that is returns {@code null} for any input argument.
     *
     * @param methodName name of the method to be validated
     * @param methodMetaData meta data of the method to be validated
     *
     * @return {@code null}, always
     */
    @Override
    public FrpcMethodValidationResult doAdditionalValidation(String methodName, FrpcMethodMetaData methodMetaData) {
        return null;
    }

}
