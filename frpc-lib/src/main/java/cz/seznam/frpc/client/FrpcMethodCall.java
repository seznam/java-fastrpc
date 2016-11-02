package cz.seznam.frpc.client;

import cz.seznam.frpc.ByteArrayFrpcMarshaller;
import cz.seznam.frpc.FrpcCallException;
import cz.seznam.frpc.FrpcDataException;
import cz.seznam.frpc.FrpcUnmarshaller;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpCookie;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcMethodCall {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcMethodCall.class);

    private Request request;
    private List<Object> implicitParameters;
    private List<HttpCookie> cookies;
    private Map<String, String> headers;
    private int maxAttemptCount;
    private long timeout;
    private TimeUnit timeoutTimeUnit;
    private long retryDelay;
    private TimeUnit retryDelayTimeUnit;
    private String method;
    private List<Object> parameters;

    FrpcMethodCall(Request request, List<Object> implicitParameters, List<HttpCookie> cookies,
                          Map<String, String> headers, int maxAttemptCount, long timeout, TimeUnit timeoutTimeUnit,
                          long retryDelay, TimeUnit retryDelayTimeUnit, String method, List<Object> parameters) {
        this.request = request;
        this.implicitParameters = implicitParameters == null ? Collections.emptyList() : implicitParameters;
        this.cookies = cookies == null ? Collections.emptyList() : cookies;
        this.headers = headers == null ? Collections.emptyMap() : headers;
        this.maxAttemptCount = maxAttemptCount;
        this.timeout = timeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
        this.retryDelay = retryDelay;
        this.retryDelayTimeUnit = retryDelayTimeUnit;
        this.method = method;
        this.parameters = parameters;
    }

    public FrpcMethodCall withImplicitParameters(Object... implicitParameters) {
        this.implicitParameters = implicitParameters == null ? null :
                Arrays.stream(implicitParameters).collect(Collectors.toList());
        return this;
    }

    public FrpcMethodCall withAddedImplicitParameters(Object... implicitParameters) {
        if(ArrayUtils.isNotEmpty(implicitParameters)) {
            Arrays.stream(implicitParameters).forEach(this.implicitParameters::add);
        }
        return this;
    }

    public FrpcMethodCall withCookies(HttpCookie... cookies) {
        this.cookies = cookies == null ? null :
                Arrays.stream(cookies).collect(Collectors.toList());
        return this;
    }

    public FrpcMethodCall withCookies(Collection<HttpCookie> cookies) {
        this.cookies = cookies == null ? null :
                cookies.stream().collect(Collectors.toList());
        return this;
    }

    public FrpcMethodCall withAddedCookies(HttpCookie... cookies) {
        if(ArrayUtils.isNotEmpty(cookies)) {
            Arrays.stream(cookies).forEach(this.cookies::add);
        }
        return this;
    }

    public FrpcMethodCall withAddedCookie(Collection<HttpCookie> cookies) {
        this.cookies = cookies == null ? null :
                cookies.stream().collect(Collectors.toList());
        return this;
    }

    public FrpcMethodCall withAddedCookies(Collection<HttpCookie> cookies) {
        this.cookies.addAll(Objects.requireNonNull(cookies));
        return this;
    }

    public FrpcMethodCall withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public FrpcMethodCall withAddedHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public FrpcMethodCall withAddedHeaders(Map<String, String> headers) {
        this.headers.putAll(Objects.requireNonNull(headers));
        return this;
    }

    public FrpcMethodCall withTimeout(long newTimeout, TimeUnit timeUnit)  {
        this.timeout = newTimeout;
        this.timeoutTimeUnit = Objects.requireNonNull(timeUnit);
        return this;
    }

    public FrpcMethodCall withAttemptCount(int newAttemptCount)  {
        this.maxAttemptCount = newAttemptCount;
        return this;
    }

    public FrpcMethodCall withRetryDelay(int newRetryDelay, TimeUnit timeUnit)  {
        this.retryDelay = newRetryDelay;
        this.retryDelayTimeUnit = Objects.requireNonNull(timeUnit);
        return this;
    }

    public UnwrappedFrpcCallResult getUnwrappedResult() {
        return doRemoteInvocation().unwrap();
    }

    public FrpcCallResult getResult() {
        return doRemoteInvocation();
    }

    private FrpcCallResult doRemoteInvocation() {
        int attempts = 0;
        FrpcCallResult output = null;

        do {
            attempts++;
            try {
                // prepare marshaller
                ByteArrayFrpcMarshaller marshaller = new ByteArrayFrpcMarshaller();
                // start content by packing method name
                marshaller.packMagic();
                marshaller.packMethodCall(method);

                // add implicit parameters
                for(Object implicitParam : implicitParameters) {
                    marshaller.packItem(implicitParam);
                }

                // marshall params given to this method
                for(Object param : parameters) {
                    marshaller.packItem(param);
                }

                // prepare the request
                prepareRequest();
                // add content provider
                request.content(new BytesContentProvider(marshaller.getBytes()));
                // send it
                ContentResponse response = request.send();

                // get the response as byte array
                byte[] responseBody = response.getContent();
                // unmarshall the response body into an object
                FrpcUnmarshaller unmarshaller = new FrpcUnmarshaller(responseBody);
                Object responseObject = unmarshaller.unmarshallObject();
                // create FRPC result out the unmarshalled response
                output =  new FrpcCallResult(responseObject, response.getStatus());
                break;
            } catch (FrpcDataException | InterruptedException | ExecutionException | TimeoutException e) {
                if(attempts == maxAttemptCount) {
                    throw new FrpcCallException("An error occurred repeatedly (" + maxAttemptCount + " times) while trying to call FRPC method " + method);
                }
                if(retryDelay > 0) {
                    try {
                        Thread.sleep(retryDelayTimeUnit.toMillis(retryDelay));
                    } catch (InterruptedException e1) {
                        LOGGER.warn("An interrupted exception occurred while waiting for {} {} before another try.", retryDelay, retryDelayTimeUnit.name());
                    }
                }
            }
        } while (attempts <= maxAttemptCount);
        // return the result
        return output;
    }

    private void prepareRequest() {
        request.timeout(timeout, timeoutTimeUnit);
        cookies.forEach(request::cookie);
        headers.forEach(request::header);
    }

}
