package cz.seznam.frpc;

import cz.seznam.frpc.client.FrpcClient;
import cz.seznam.frpc.client.FrpcConfig;

public class FrpcDemoClient
{
    public static void main( String[] args )
    {
        // Configuration of FastRPC connection
        FrpcConfig frpcConfig = new FrpcConfig("localhost", 9898, "/RPC2");

        // Creation of FastRPC client
        FrpcClient frpcClient = new FrpcClient(frpcConfig);

        Object result = null;

        try {
            // Calling of remote method via FastRPC client
            // Method name: indexOf
            // Parameters: the string to search, the substring to search for
            // Returns: the array of indexes of occurrences of the specified substring
            result = frpcClient.call("operations.multiply", 3, 2);
        } catch (FrpcConnectionException e) {
            e.printStackTrace();
        } catch (FrpcDataException e) {
            e.printStackTrace();
        }

        FrpcLog.d("", "Result: %s", FrpcLog.frpcToString(result));
    }
}
