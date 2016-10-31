package cz.seznam.frpc.handlers;

import cz.seznam.frpc.FrpcResponse;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ArrayOperations {

    @FrpcResponse(key = "result")
    public int indexOf(int element, Object[] array) {
        return Arrays.stream(array).collect(Collectors.toList()).indexOf(element);
    }

}
