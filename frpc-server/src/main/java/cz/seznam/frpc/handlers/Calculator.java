package cz.seznam.frpc.handlers;

import cz.seznam.frpc.common.FrpcResponseUtils;
import cz.seznam.frpc.server.annotations.FrpcIgnore;
import cz.seznam.frpc.server.annotations.FrpcMethod;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Just a simple example class publishing implementing some {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class Calculator {

    private AtomicLong addCallCount = new AtomicLong(0);

    @FrpcMethod(name = "add", resultKey = "result")
    public long plus(long i, long j) {
        addCallCount.incrementAndGet();
        return i + j;
    }

    @FrpcMethod(name = "subtract", resultKey = "result")
    public long minus(long i, long j) {
        return i - j;
    }

    @FrpcMethod(name = "multiply", resultKey = "result")
    public Map<String, Object> times(long i, long j) {
        long result = i * j;
        Map<String, Object> output = FrpcResponseUtils.ok("No problem ;)");
        output.put("multiplication", result);
        return output;
    }

    @FrpcIgnore
    public long divide(long i, long j) {
        return i / j;
    }

    @FrpcMethod(name = "addCallCount", resultKey = "count")
    public long getAddCallCount() {
        return addCallCount.get();
    }

}
