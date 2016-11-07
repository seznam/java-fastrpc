package cz.seznam.frpc.handlers;

import cz.seznam.frpc.server.annotations.FrpcMethod;

/**
 * Just a simple example class publishing implementing some {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryOperations {

    @FrpcMethod(name = "bytesToString", resultKey = "result")
    public String toString(byte[] bytes) {
        return new String(bytes);
    }

    @FrpcMethod(name = "stringToBytes", resultKey = "result")
    public byte[] toBytes(String text) {
        return text.getBytes();
    }

    @FrpcMethod(name = "/dev/null", resultKey = "result")
    public Void blackhole(Object whatever) {
        return null;
    }

}
