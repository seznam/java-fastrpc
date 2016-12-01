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

    public Map<String, List<Map<String, List<String>>>> getComplexValue() {
        Map<String, List<Map<String, List<String>>>> map = new HashMap<>();
        List<Map<String, List<String>>> list = new ArrayList<>();
        Map<String, List<String>> nestedMap = new HashMap<>();
        List<String> values = Arrays.asList("some", "values", "in", "a", "list");

        nestedMap.put("values", values);
        list.add(nestedMap);
        map.put("topLevelKey", list);

        return map;
    }

    public List<String> flatten(ConcurrentSkipListMap<String, LinkedHashSet<List<String>>[]>[] value) {
        return Arrays.stream(value).flatMap(e -> e.values().stream()).flatMap(Arrays::stream)
                .flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public Integer getFaultyInteger() {
        throw new RuntimeException("No integer for you!");
    }

}
