package cz.sklik.frpc;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;

public class FrpcDemoServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9898), 0);
        server.createContext("/RPC2", new FrpcHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class FrpcHandler implements HttpHandler {

        public void handle(HttpExchange t) throws IOException {

            try {
                // Request unmarshalling
                FrpcBinUnmarshaller unmarshaller = new FrpcBinUnmarshaller(t.getRequestBody());
                // Method dispatch and response retrieving
                Object rpcResponse = dispatchMethod(unmarshaller);

                // Response marshalling
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FrpcBinMarshaller marshaller = new FrpcBinMarshaller(baos);
                marshaller.packMagic();
                marshaller.packItem(rpcResponse);

                // Setting HTTP response header
                Headers responseHeaders = t.getResponseHeaders();
                responseHeaders.set("Content-Type", "application/x-frpc");

                // Setting HTTP response status and size
                t.sendResponseHeaders(200, baos.size());

                // Writing HTTP response body
                OutputStream os = t.getResponseBody();
                os.write(baos.toByteArray());
                os.close();
            } catch (FrpcDataException e) {
                t.sendResponseHeaders(500, 0);
            }
        }
    }

    private static Object dispatchMethod(FrpcBinUnmarshaller unmarshaller) throws FrpcDataException {
        String method = unmarshaller.unmarshallMethodName();
        if (method.equals("indexOf")) {
            return dispatchIndexOf(unmarshaller);
        }
        return rpcFail(404, "Unknown method.");
    }

    private static Object dispatchIndexOf(FrpcBinUnmarshaller unmarshaller) throws FrpcDataException {
        Object str = unmarshaller.unmarshallObject();
        Object sub = unmarshaller.unmarshallObject();

        if (!(str instanceof String)) {
            return rpcFail(400, "Unexpected type of the 1st parameter. String is expected here.");
        }

        if (!(sub instanceof String)) {
            return rpcFail(400, "Unexpected type of the 2ns parameter. String is expected here.");
        }

        return indexOf((String) str, (String) sub);
    }

    private static List<Integer> indexOf(String str, String sub) {
        List<Integer> positions = new LinkedList<>();

        int fromIndex = 0;

        do {
            fromIndex = str.indexOf(sub, fromIndex + 1);
            positions.add(fromIndex);
        } while (fromIndex > -1);

        return positions;
    }

    private static Map<String, Object> rpcFail(final int statusCode, final String message) {
        return new HashMap<String, Object>() {{
            put("status", statusCode);
            put("statusMessage", message);
        }};
    }
}
