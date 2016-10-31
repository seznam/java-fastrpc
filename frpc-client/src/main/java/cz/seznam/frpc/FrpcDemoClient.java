package cz.seznam.frpc;

import cz.seznam.frpc.client.FrpcClient;
import cz.seznam.frpc.client.FrpcConfig;

import java.util.Arrays;
import java.util.HashMap;

public class FrpcDemoClient
{
    public static void main( String[] args )
    {
        // Configuration of FastRPC connection
        FrpcConfig frpcConfig = new FrpcConfig("localhost", 9898, "/RPC2");

        // Creation of FastRPC client
        FrpcClient frpcClient = new FrpcClient(frpcConfig);

        try {
            // Calling of remote method via FastRPC client
            // Method name: indexOf
            // Parameters: the string to search, the substring to search for
            // Returns: the array of indexes of occurrences of the specified substring
            Object stringResult = frpcClient.call("stringOperations.indexOf", "testStringValue", "String");
            System.out.println(FrpcLog.frpcToString(stringResult));

            Object numberResult = frpcClient.call("numberOperations.multiply", 3, 2);
            System.out.println(FrpcLog.frpcToString(numberResult));

            Object arraysResult = frpcClient.call("arrayOperations.indexOf", 1, new int[] {5, 6, 7, 2, 1, 6});
            System.out.println(FrpcLog.frpcToString(arraysResult));

            Object listResult = frpcClient.call("collectionOperations.removeNulls", Arrays.asList("x", null, "y", null, "z", null));
            System.out.println(FrpcLog.frpcToString(listResult));

            Object setResult = frpcClient.call("collectionOperations.sort", Arrays.asList("x", "a", "c", "b"));
            System.out.println(FrpcLog.frpcToString(setResult));

            Object mapResult = frpcClient.call("collectionOperations.putIfAbsent", new HashMap<>(), "test", "someValue");
            System.out.println(FrpcLog.frpcToString(mapResult));
        } catch (FrpcConnectionException e) {
            e.printStackTrace();
        } catch (FrpcDataException e) {
            e.printStackTrace();
        }
    }
}
