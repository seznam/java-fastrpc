package cz.seznam.frpc.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Just a simple example class implementing some {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ArrayOperations {

    public int indexOf(int element, int[] array) {
        return Arrays.stream(array).boxed().collect(Collectors.toList()).indexOf(element);
    }

    public List getFirst(List[] array) {
        return array[0];
    }

}
