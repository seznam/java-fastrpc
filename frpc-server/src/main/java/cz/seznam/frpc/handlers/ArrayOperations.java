package cz.seznam.frpc.handlers;

import cz.seznam.frpc.server.annotations.FrpcMethod;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Just a simple example class publishing implementing a {@code FRPC} method.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ArrayOperations {

    @FrpcMethod(resultKey = "result")
    public int indexOf(int element, Object[] array) {
        return Arrays.stream(array).collect(Collectors.toList()).indexOf(element);
    }

}
