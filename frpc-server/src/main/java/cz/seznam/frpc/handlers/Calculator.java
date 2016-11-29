package cz.seznam.frpc.handlers;

import cz.seznam.frpc.core.FrpcResponseUtils;
import cz.seznam.frpc.server.annotations.FrpcIgnore;
import cz.seznam.frpc.server.annotations.FrpcMethod;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Just a simple example class implementing some {@code FRPC} methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class Calculator {

    /**
     * Used to demonstrate that handlers can be stateful.
     */
    private AtomicLong addCallCount = new AtomicLong(0);

    @FrpcMethod("add")
    public long plus(long i, long j) {
        addCallCount.incrementAndGet();
        return i + j;
    }

    @FrpcMethod("subtract")
    public long minus(long i, long j) {
        return i - j;
    }

    @FrpcMethod("multiply")
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

    @FrpcMethod("addCallCount")
    public long getAddCallCount() {
        return addCallCount.get();
    }

}
