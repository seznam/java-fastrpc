package cz.seznam.frpc;

import cz.seznam.frpc.client.FrpcClient;
import org.eclipse.jetty.client.HttpClient;

import java.net.URL;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class FrpcDemoClient
{
    public static void main( String[] args ) throws Exception {
        URL url = new URL("http://localhost:9898/RPC2");
        HttpClient httpClient = new HttpClient();
        // Creation of FastRPC client
        FrpcClient client = new FrpcClient(url, httpClient);


        try {
            // unwrapping takes care of single-valued responses
            Integer sum = client.call("numberOperations.add", 3, 2).unwrap().as(Integer.class);
            System.out.println(sum);

            // and there is even a shorthand for it
            Integer sum2 = client.callAndUnwrap("numberOperations.add", 3, 2).as(Integer.class);
            System.out.println(sum2);

            // yet it's not mandatory, this is equivalent to previous call
            Integer sum3 = client.call("numberOperations.add", 3, 2).asStruct().get("result").as(Integer.class);
            System.out.println(sum3);

            // nested structures can be retrieved using "get" method of structured result
            // "getStruct" comes in handy when the value to get is again a structure
            Integer multiplication = client.call("numberOperations.multiply", 3, 2)
                    .asStruct().getStruct("result").get("multiplication").as(Integer.class);
            System.out.println(multiplication);

            // nothing special here, just a call...
            Integer index = client.call("arrayOperations.indexOf", 1, new int[] {5, 6, 7, 2, 1, 6})
                    .unwrap().as(Integer.class);
            System.out.println(index);

            // with asArrayOf one can specify type of an array so that it can be returned in a type-safe way
            String[] withoutNulls = client.call("collectionOperations.removeNulls", Arrays.asList("x", null, "y", null, "z", null))
                    .unwrap().asArrayOf(String.class).asArray();
            System.out.println(Arrays.toString(withoutNulls));

            // arrays can be implicitly converted to Lists...
            List<String> sorted = client.call("collectionOperations.sort", Arrays.asList("x", "a", "c", "b"))
                    .unwrap().asArrayOf(String.class).asList();
            System.out.println(sorted);

            // ... Sets ...
            Set<String> sortedAsSet = client.call("collectionOperations.sort", Arrays.asList("x", "a", "c", "b"))
                    .unwrap().asArrayOf(String.class).asSet();
            System.out.println(sortedAsSet);

            // ... and pretty much any other collection type
            LinkedBlockingDeque<String> sortedAsDequeue = client.call("collectionOperations.sort", Arrays.asList("x", "a", "c", "b"))
                    .unwrap().asArrayOf(String.class).asCollection(LinkedBlockingDeque::new);
            System.out.println(sortedAsDequeue);

            // maps can be retrieved from structured response directly
            Map<String, Object> map = client.call("collectionOperations.putIfAbsent", new HashMap<>(), "test", "someValue")
                    .unwrap().asStruct().asMap();
            System.out.println(map);

            // binary data are just binary
            String binaryResult = client.call("binaryOperations.bytesToString", (Object) "Binary data are just so ".getBytes())
                    .unwrap().as(String.class);
            System.out.print(binaryResult);

            byte[] binaryResult2 = client.call("binaryOperations.stringToBytes", "goddamn binary!")
                    .unwrap().as(byte[].class);
            System.out.println(new String(binaryResult2));

        } finally {
            httpClient.stop();
        }
    }
}
