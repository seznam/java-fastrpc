package cz.seznam.frpc.handlers;

import cz.seznam.frpc.FrpcResponse;

import java.util.*;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class CollectionOperations {

    @FrpcResponse(key = "result")
    public Map<String, Object> putIfAbsent(Map<String, Object> map, String key, Object value) {
        map.putIfAbsent(key, value);
        return map;
    }

    @FrpcResponse(key = "result")
    public List<String> sort(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    @FrpcResponse(key = "result")
    public List<String> removeNulls(List<String> list) {
        list.removeIf(e -> e == null);
        return list;
    }

}
