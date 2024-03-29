package cz.seznam.frpc.handlers;

import java.util.LinkedList;
import java.util.List;

/**
 * Just a simple example class publishing implementing some {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class StringOperations {

    public List<Integer> indexOf(String str, String sub) {
        List<Integer> positions = new LinkedList<>();

        int fromIndex = 0;

        while (true) {
            fromIndex = str.indexOf(sub, fromIndex + 1);
            if(fromIndex > -1) {
                positions.add(fromIndex);
            } else {
                break;
            }
        }

        return positions;
    }

    public String sub(String source, int from, int to) {
        return source.substring(from, to);
    }

}
