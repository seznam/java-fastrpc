package cz.seznam.frpc;

import cz.seznam.frpc.client.FrpcClient;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class FrpcDemoClient
{
    public static void main( String[] args ) {
        // Creation of FastRPC client
        FrpcClient client = new FrpcClient("http://localhost:9898/RPC2");

        // unwrapping takes care of single-valued responses
        Long sum = client.call("numberOperations.add", 3, 2).unwrap().as(Long.class);
        System.out.println("Result of numberOperations.add using unwrap(): " + sum);

        // and there is even a shorthand for it
        Long sum2 = client.callAndUnwrap("numberOperations.add", 3, 2).as(Long.class);
        System.out.println("Result of numberOperations.add using callAndUnwrap(): " + sum2);

        // yet it's not mandatory, this is equivalent to previous call
        Long sum3 = client.call("numberOperations.add", 3, 2).asStruct().get("result").as(Long.class);
        System.out.println("Result of numberOperations.add using asStruct().get(): " + sum3);

        // nested structures can be retrieved using "get" method of structured result
        // "getStruct" comes in handy when the name to get is again a structure
        Long multiplication = client.call("numberOperations.multiply", 3, 2)
                .asStruct().getStruct("result").get("multiplication").as(Long.class);
        System.out.println("Result of numberOperations.multiply (multiple nesting): " + multiplication);

        // nothing special here, just a call...
        Integer index = client.call("arrayOperations.indexOf", 1, new int[] {5, 6, 7, 2, 1, 6})
                .unwrap().as(Integer.class);
        System.out.println("Result of arrayOperations.indexOf: " + index);

        // with asArrayOf one can specify type of an array so that it can be returned in a type-safe way
        String[] withoutNulls = client.call("collectionOperations.removeNulls", Arrays.asList("x", null, "y", null, "z", null))
                .unwrap().asArrayOf(String.class).asArray();
        System.out.println("Result of collectionOperations.removeNulls returned as String[]: " + Arrays.toString(withoutNulls));

        // arrays can be implicitly converted to Lists...
        List<String> sorted = client.call("collectionOperations.sort", Arrays.asList("x", "a", "c", "b"))
                .unwrap().asArrayOf(String.class).asList();
        System.out.println("Result of collectionOperations.sort returned as List<String>: " + sorted);

        // ... Sets ...
        Set<String> sortedAsSet = client.call("collectionOperations.sort", Arrays.asList("x", "a", "c", "b"))
                .unwrap().asArrayOf(String.class).asSet();
        System.out.println("Result of collectionOperations.sort returned as Set<String>: " + sortedAsSet);

        // ... and pretty much any other collection type
        LinkedBlockingDeque<String> sortedAsDequeue = client.call("collectionOperations.sort", Arrays
                .asList("x", "a", "c", "b"))
                .unwrap().asArrayOf(String.class).asCollection(LinkedBlockingDeque::new);
        System.out.println("Result of collectionOperations.sort returned as custom collection type of Strings (LinkedBlockingDeque): " + sortedAsDequeue);

        // maps can be retrieved from structured response directly
        Map<String, Object> map = client.call("collectionOperations.putIfAbsent", new HashMap<>(), "test", "someValue")
                .unwrap().asStruct().asMap();
        System.out.println("Result of collectionOperations.putIfAbsent returned as Map<String, Object> using asStruct().asMap(): " + map);

        // binary data are just binary
        String binaryResult = client.call("binaryOperations.bytesToString", (Object) "Some binary data FRPC method input".getBytes())
                .unwrap().as(String.class);
        System.out.println("Result of binaryOperations.bytesToString: " + binaryResult);

        byte[] binaryResult2 = client.call("binaryOperations.stringToBytes", "Some binary data as FRPC method result")
                .unwrap().as(byte[].class);
        System.out.println("Result of binaryOperations.stringToBytes: " + Arrays.toString(binaryResult2));

        // nulls are just nulls
        Void nullResult = client.call("binaryOperations./dev/null", "String to be obliterated")
                .unwrap().as(Void.class);
        System.out.println("Result of binaryOperations./dev/null: " + nullResult);

    }
}
