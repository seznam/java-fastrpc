package cz.seznam.frpc.core;

/**
 * Result of conversion of an object to given type.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ConversionResult {

    private final boolean success;
    private final String errorMessage;
    private final Object converted;

    ConversionResult(boolean success, String errorMessage, Object converted) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.converted = converted;
    }

    /**
     * Indicates success or failure of the conversion.
     *
     * @return {@code true} if the object could be converted into desired type and {@code false} otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns error message produces by the converter in case of conversion failure.
     *
     * @return error message produces by the converter in case of conversion failure
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the converted object or {@code null} if the conversion failed.
     *
     * @return the converted object or {@code null} if the conversion failed
     */
    public Object getConverted() {
        return converted;
    }
}
