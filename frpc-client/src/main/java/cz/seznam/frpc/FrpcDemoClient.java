package cz.seznam.frpc;

import cz.seznam.frpc.client.FrpcCallResult;
import cz.seznam.frpc.client.FrpcClient;
import cz.seznam.frpc.client.FrpcFaultException;
import cz.seznam.frpc.core.FrpcType;
import cz.seznam.frpc.core.transport.FrpcFault;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

public class FrpcDemoClient {
    public static void main(String[] args) {

        /* CLIENT CREATION */

        // create new FRPC client using builder-style API
        FrpcClient client = FrpcClient.builder()
                .url("http://localhost:9898/RPC2")
                .usingDefaultHttpClient()
                .build();

        /* CALLING METHODS AND GETTING RESULTS AS POJOS */

        // this is what a simple call looks like
        Long sum = client.call("numberOperations.add", 3, 2).as(Long.class);
        System.out.println("Result of numberOperations.add using unwrap(): " + sum);

        // nested structures can be retrieved using "get" method of structured result
        // "getStruct" comes in handy when the value to get is again a structure
        Long multiplication = client.call("numberOperations.multiply", 3, 2).asStruct().get("multiplication")
                .as(Long.class);
        System.out.println("Result of numberOperations.multiply (multiple nesting): " + multiplication);

        // nothing special here, just a call...
        Integer index = client.call("arrayOperations.indexOf", 1, new int[]{5, 6, 7, 2, 1, 6}).as(Integer.class);
        System.out.println("Result of arrayOperations.indexOf: " + index);

        // array containing other complex types work too
        List<Integer[]> list = client.call("arrayOperations.getFirst",
                (Object) new List[]{Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3, 4)), Arrays.asList(
                        Arrays.asList(5, 6), Arrays.asList(7, 8))}).asArrayOf(Integer[].class).asList();
        System.out.println("Result of arrayOperations.getFirst: " + list.stream().map(Arrays::toString).collect(
                Collectors.joining(",", "[", "]")));

        // with asArrayOf one can specify type of an array so that it can be returned in a type-safe way
        String[] withoutNulls = client
                .call("collectionOperations.removeNulls", Arrays.asList("x", null, "y", null, "z", null))
                .asArrayOf(String.class).asArray();
        System.out.println(
                "Result of collectionOperations.removeNulls returned as String[]: " + Arrays.toString(withoutNulls));

        // arrays can be implicitly converted to Lists...
        List<String> sorted = client.call("collectionOperations.sort", Arrays.asList("x", "a", "c", "b"))
                .asArrayOf(String.class).asList();
        System.out.println("Result of collectionOperations.sort returned as List<String>: " + sorted);

        // ... Sets ...
        Set<String> sortedAsSet = client.call("collectionOperations.sort", Arrays.asList("x", "a", "c", "b"))
                .asArrayOf(String.class).asSet();
        System.out.println("Result of collectionOperations.sort returned as Set<String>: " + sortedAsSet);

        // ... and pretty much any other collection type
        LinkedBlockingDeque<String> sortedAsDequeue = client.call("collectionOperations.sort",
                Arrays.asList("x", "a", "c", "b")).asArrayOf(String.class).asCollection(LinkedBlockingDeque::new);
        System.out.println(
                "Result of collectionOperations.sort returned as custom collection type of Strings (LinkedBlockingDeque): " + sortedAsDequeue);

        // maps can be retrieved from structured response directly
        Map<String, Object> map = client.call("collectionOperations.putIfAbsent", new HashMap<>(), "test", "someValue")
                .asStruct().asMap();
        System.out.println(
                "Result of collectionOperations.putIfAbsent returned as Map<String, Object> using asStruct().asMap(): " + map);

        // binary data are just binary
        String binaryResult = client
                .call("binaryOperations.bytesToString", (Object) "Some binary data FRPC method input".getBytes())
                .as(String.class);
        System.out.println("Result of binaryOperations.bytesToString: " + binaryResult);

        byte[] binaryResult2 = client.call("binaryOperations.stringToBytes", "Some binary data as FRPC method result")
                .as(byte[].class);
        System.out.println("Result of binaryOperations.stringToBytes: " + Arrays.toString(binaryResult2));

        // nulls are just nulls
        Object nullResult = client.call("binaryOperations./dev/null", "String to be obliterated")
                .asObject();
        System.out.println("Result of binaryOperations./dev/null: " + nullResult);

        // complex types work as well
        List<String> l1 = Arrays.asList("1", "2");
        List<String> l2 = Arrays.asList("3", "4");
        Set<List<String>> s = new HashSet<>();
        s.add(l1);
        s.add(l2);

        List<String> l3 = Arrays.asList("ahoj", "test");
        List<String> l4 = Arrays.asList("string", "StrinG");
        Set<List<String>> s2 = new HashSet<>();
        s2.add(l3);
        s2.add(l4);

        Map<String, List<Set<List<String>>>> m = new HashMap<>();
        m.put("first", Arrays.asList(s, s2));
        m.put("second", Arrays.asList(s, s2));

        Object[] array = new Object[]{m, m};


        List<String> flattenResult = client.call("otherOperations.flatten", (Object) array)
                .asArrayOf(String.class).asList();
        System.out.println("Result of otherOperations.flatten: " + flattenResult);

        // complex objects can be mapped in a type safe way
        Map<String, List<Map<String, List<String>>>> complexResult = client.call("otherOperations.getComplexValue")
                .as(new FrpcType<Map<String, List<Map<String, List<String>>>>>() {
                });
        System.out.println("Result of otherOperations.getComplexValue: " + complexResult);

        /* ERROR HANDLING */

        // errors can be handler either by explicitly checking for them
        FrpcCallResult result = client.call("otherOperations.getFaultyInteger");
        if(result.isFault()) {
            printFault(result.asFault());
        }

        // or by trying to get result as desired type and catching FrpcFaultException...
        try {
            int faultyInt = client.call("otherOperations.getFaultyInteger").as(int.class);
            System.out.println("Contemplating what to do with int value " + faultyInt);
        } catch (FrpcFaultException e) {
            // ... from which the fault can be extracted
            printFault(e.getFault());
        }

    }

    private static void printFault(FrpcFault fault) {
        System.out.println("There was an error while calling FRPC method \"otherOperations.getFaultyInteger\", " +
                "status code: " + fault.getStatusCode() + ", status message: " + fault.getStatusMessage());
    }


}
