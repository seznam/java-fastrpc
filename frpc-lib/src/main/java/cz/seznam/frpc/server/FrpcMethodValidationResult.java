package cz.seznam.frpc.server;

/**
 * Result of {@code FRPC} method validation.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcMethodValidationResult {

    private final boolean isValid;
    private final String error;

    /**
     * Creates new instance from given parameters.
     *
     * @param isValid whether or not the method is valid
     * @param error error message describing the problem in case the method is not valid
     */
    public FrpcMethodValidationResult(boolean isValid, String error) {
        this.isValid = isValid;
        this.error = error;
    }

    /**
     * Returns whether or not the method is valid.
     *
     * @return boolean value indicating whether or not the method is valid
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Returns error message describing the problem in case the method is not valid.
     *
     * @return error message describing the problem in case the method is not valid
     */
    public String getError() {
        return error;
    }

}
