package cz.seznam.frpc.handlers;

import cz.seznam.frpc.FrpcIgnore;
import cz.seznam.frpc.FrpcName;
import cz.seznam.frpc.FrpcResponse;
import cz.seznam.frpc.FrpcUtils;

import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class Calculator {

    @FrpcName("add")
    @FrpcResponse(key = "result")
    public long plus(long i, long j) {
        return i + j;
    }

    @FrpcName("subtract")
    @FrpcResponse(key = "result")
    public long minus(long i, long j) {
        return i - j;
    }

    @FrpcName("multiply")
    public Map<String, Object> times(long i, long j) {
        long result = i * j;
        Map<String, Object> output = FrpcUtils.ok("No problem ;)");
        output.put("multiplication", result);
        return output;
    }

    @FrpcIgnore
    public long divide(long i, long j) {
        return i / j;
    }

}
