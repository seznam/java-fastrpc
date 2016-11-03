package cz.seznam.frpc.client;

import cz.seznam.frpc.common.ByteArrayFrpcMarshaller;
import cz.seznam.frpc.common.FrpcDataException;
import cz.seznam.frpc.common.FrpcUnmarshaller;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcMethodCall {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcMethodCall.class);

    private HttpClient client;
    private HttpPost request;
    private List<Object> implicitParameters;
    private Map<String, String> headers;
    private int maxAttemptCount;
    private Long connectTimeout;
    private TimeUnit connectTimeoutTimeUnit;
    private Long socketTimeout;
    private TimeUnit socketTimeoutTimeUnit;
    private long retryDelay;
    private TimeUnit retryDelayTimeUnit;
    private String method;
    private List<Object> parameters;

    FrpcMethodCall(HttpClient client, HttpPost request, List<Object> implicitParameters,
                   Map<String, String> headers, int maxAttemptCount, long retryDelay, TimeUnit retryDelayTimeUnit,
                   String method, List<Object> parameters) {
        this.client = client;
        this.request = request;
        this.implicitParameters = implicitParameters == null ? Collections.emptyList() : implicitParameters;
        this.headers = headers == null ? Collections.emptyMap() : headers;
        this.maxAttemptCount = maxAttemptCount;
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

    public FrpcMethodCall withConnectTimeout(long newTimeout, TimeUnit timeUnit)  {
        this.connectTimeout = newTimeout;
        this.connectTimeoutTimeUnit = Objects.requireNonNull(timeUnit);
        return this;
    }

    public FrpcMethodCall withSocketTimeout(long newTimeout, TimeUnit timeUnit) {
        this.socketTimeout = newTimeout;
        this.socketTimeoutTimeUnit = Objects.requireNonNull(timeUnit);
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
                // set body
                request.setEntity(new ByteArrayEntity(marshaller.getBytes()));
                // send it
                HttpResponse response = client.execute(request);

                // unmarshall the response body into an object
                InputStream body = response.getEntity().getContent();
                try {
                    FrpcUnmarshaller unmarshaller = new FrpcUnmarshaller(body);
                    Object responseObject = unmarshaller.unmarshallObject();
                    // create FRPC result out the unmarshalled response
                    output =  new FrpcCallResult(responseObject, response.getStatusLine().getStatusCode());
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
                // done, break the cycle
                break;
            } catch (FrpcDataException | IOException e) {
                if(attempts == maxAttemptCount) {
                    throw new FrpcCallException("An error occurred repeatedly (" + maxAttemptCount + " times) while trying to call FRPC method " + method, e);
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
        // set timeouts
        if(connectTimeout != null || socketTimeout != null) {
            RequestConfig.Builder configBuilder = RequestConfig.custom();
            if(connectTimeout != null) {
                configBuilder.setConnectTimeout((int) connectTimeoutTimeUnit.toMillis(connectTimeout));
            }
            if(socketTimeout != null) {
                configBuilder.setSocketTimeout((int) socketTimeoutTimeUnit.toMillis(socketTimeout));
            }
            request.setConfig(configBuilder.build());
        }
        // set headers
        headers.forEach(request::setHeader);
    }

}
