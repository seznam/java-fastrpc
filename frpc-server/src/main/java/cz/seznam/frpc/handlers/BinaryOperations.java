package cz.seznam.frpc.handlers;

import cz.seznam.frpc.server.annotations.FrpcName;
import cz.seznam.frpc.server.annotations.FrpcResponse;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class BinaryOperations {

    @FrpcName("bytesToString")
    @FrpcResponse(key = "result")
    public String toString(byte[] bytes) {
        return new String(bytes);
    }

    @FrpcName("stringToBytes")
    @FrpcResponse(key = "result")
    public byte[] toBytes(String text) {
        return text.getBytes();
    }

    @FrpcName("/dev/null")
    @FrpcResponse(key = "result")
    public Void blackhole(Object whatever) {
        return null;
    }

}
