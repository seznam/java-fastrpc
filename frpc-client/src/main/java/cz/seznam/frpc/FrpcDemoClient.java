package cz.seznam.frpc;

import cz.seznam.frpc.client.FrpcClient;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

public class FrpcDemoClient
{
    public static void main( String[] args ) {
        // Creation of FastRPC client
        FrpcClient client = new FrpcClient("http://localhost:9898/RPC2");

        // this is what a simple call looks like
        Long sum = client.call("numberOperations.add", 3, 2).as(Long.class);
        System.out.println("Result of numberOperations.add using unwrap(): " + sum);

        // nested structures can be retrieved using "get" method of structured result
        // "getStruct" comes in handy when the name to get is again a structure
        Long multiplication = client.call("numberOperations.multiply", 3, 2).asStruct().get("multiplication")
                .as(Long.class);
        System.out.println("Result of numberOperations.multiply (multiple nesting): " + multiplication);

        // nothing special here, just a call...
        Integer index = client.call("arrayOperations.indexOf", 1, new int[] {5, 6, 7, 2, 1, 6}).as(Integer.class);
        System.out.println("Result of arrayOperations.indexOf: " + index);

        // array containing other complex types work too
        List<Object[]> list = client.call("arrayOperations.getFirst",
                (Object) new List[]{Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3, 4)), Arrays.asList(
                        Arrays.asList(5, 6), Arrays.asList(7, 8))}).asArrayOf(Object[].class).asList();
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
        Void nullResult = client.call("binaryOperations./dev/null", "String to be obliterated")
                .as(Void.class);
        System.out.println("Result of binaryOperations./dev/null: " + nullResult);

    }
}
