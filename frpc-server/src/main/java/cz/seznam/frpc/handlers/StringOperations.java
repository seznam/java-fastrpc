package cz.seznam.frpc.handlers;

import cz.seznam.frpc.server.annotations.FrpcResponse;

import java.util.LinkedList;
import java.util.List;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class StringOperations {

    @FrpcResponse(key = "result")
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

    @FrpcResponse(key = "substring")
    public String sub(String source, int from, int to) {
        return source.substring(from, to);
    }

}
