package cz.seznam.frpc.handlers;

import cz.seznam.frpc.server.annotations.FrpcMethod;

import java.util.*;

/**
 * Just a simple example class publishing implementing some {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class CollectionOperations {

    @FrpcMethod(resultKey = "result")
    public Map<String, Object> putIfAbsent(Map<String, Object> map, String key, Object value) {
        map.putIfAbsent(key, value);
        return map;
    }

    @FrpcMethod(resultKey = "result")
    public List<String> sort(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    @FrpcMethod(resultKey = "result")
    public List<String> removeNulls(List<String> list) {
        list.removeIf(e -> e == null);
        return list;
    }

}
