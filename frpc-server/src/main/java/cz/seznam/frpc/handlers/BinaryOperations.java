package cz.seznam.frpc.handlers;

import cz.seznam.frpc.server.annotations.FrpcMethod;

/**
 * Just a simple example class implementing some {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryOperations {

    @FrpcMethod("bytesToString")
    public String toString(byte[] bytes) {
        return new String(bytes);
    }

    @FrpcMethod("stringToBytes")
    public byte[] toBytes(String text) {
        return text.getBytes();
    }

    @FrpcMethod("/dev/null")
    public Object blackhole(Object whatever) {
        return null;
    }

}
