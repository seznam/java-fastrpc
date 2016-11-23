package cz.seznam.frpc.handlers;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * Just a simple example class implementing some {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class OtherOperations {

    public Map<String, Object> getComplexValue() {
        Map<String, Object> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        Map<String, Object> nestedMap = new HashMap<>();
        List<Object> values = Arrays.asList("some", "values", "in", "a", "list");

        nestedMap.put("values", values);
        list.add(nestedMap);
        map.put("topLevelKey", list);

        return map;
    }

    public List<String> flatten(ConcurrentSkipListMap<String, LinkedHashSet<List<String>>[]>[] value) {
        return Arrays.stream(value).flatMap(e -> e.values().stream()).flatMap(Arrays::stream)
                .flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toList());
    }

}
