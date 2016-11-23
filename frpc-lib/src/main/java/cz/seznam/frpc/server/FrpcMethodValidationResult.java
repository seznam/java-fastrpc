package cz.seznam.frpc.server;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcMethodValidationResult {

    private final boolean isValid;
    private final String error;

    public FrpcMethodValidationResult(boolean isValid, String error) {
        this.isValid = isValid;
        this.error = error;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getError() {
        return error;
    }

}
