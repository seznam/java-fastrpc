package cz.seznam.frpc.core;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ConversionResult {
    private final boolean success;
    private final String errorMessage;
    private final Object converted;

    public ConversionResult(boolean success, String errorMessage, Object converted) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.converted = converted;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Object getConverted() {
        return converted;
    }
}
