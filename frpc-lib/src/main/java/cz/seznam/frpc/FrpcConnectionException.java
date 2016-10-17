package cz.seznam.frpc;

public class FrpcConnectionException extends Exception {

    private static final long serialVersionUID = 1L;

    private String message;

    public FrpcConnectionException(String msg) {
        message = msg;
    }

    @Override
    public String toString() {
        return "Frpc connection exception: " + message;
    }
}
