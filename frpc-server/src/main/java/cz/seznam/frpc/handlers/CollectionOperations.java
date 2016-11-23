package cz.seznam.frpc.handlers;

import java.util.*;

/**
 * Just a simple example class implementing some {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class CollectionOperations {

    public Map<String, Object> putIfAbsent(Map<String, Object> map, String key, Object value) {
        map.putIfAbsent(key, value);
        return map;
    }

    public List<String> sort(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    public List<String> removeNulls(List<String> list) {
        list.removeIf(e -> e == null);
        return list;
    }

}
