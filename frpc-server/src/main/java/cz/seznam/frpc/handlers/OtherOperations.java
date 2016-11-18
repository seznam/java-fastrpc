package cz.seznam.frpc.handlers;

import java.util.*;

/**
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


}
