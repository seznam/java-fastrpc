package cz.seznam.frpc;

public class FrpcDataException extends Exception {

    private static final long serialVersionUID = 8783225693772208282L;

    private String message;

    public FrpcDataException(String msg) {
        message = msg;
    }

    public String toString() {
        return "FrpcDataException" + message;
    }
}
