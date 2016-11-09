package cz.seznam.frpc.client;

import cz.seznam.frpc.core.transport.FrpcRequest;
import cz.seznam.frpc.core.transport.FrpcRequestWriter;
import cz.seznam.frpc.core.transport.FrpcResponseReader;
import cz.seznam.frpc.core.transport.Protocol;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
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
    private Protocol protocol;
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

    FrpcMethodCall(HttpClient client, HttpPost request, Protocol protocol, List<Object> implicitParameters,
                   Map<String, String> headers, int maxAttemptCount, long retryDelay, TimeUnit retryDelayTimeUnit,
                   String method, List<Object> parameters) {
        this.client = client;
        this.request = request;
        this.protocol = protocol;
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
                // get FrpcRequestWriter for current protocol
                FrpcRequestWriter requestWriter = FrpcRequestWriter.forProtocol(protocol);
                // create FrpcRequest
                FrpcRequest frpcRequest = new FrpcRequest(method, implicitParameters, parameters);
                // write it
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                requestWriter.write(frpcRequest, baos);

                // prepare the request
                prepareRequest();
                // set body
                request.setEntity(new ByteArrayEntity(baos.toByteArray()));
                // send it
                HttpResponse response = client.execute(request);

                // get response reader for current protocol
                FrpcResponseReader responseReader = FrpcResponseReader.forProtocol(protocol);
                // get response body and content length
                InputStream body = response.getEntity().getContent();
                long contentLength = response.getEntity().getContentLength();
                try {
                    // unmarshall the response body into an object
                    Object responseObject = responseReader.read(body, contentLength);
                    // create FRPC result out the unmarshalled response
                    output = new FrpcCallResult(responseObject, response.getStatusLine().getStatusCode());
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
                // done, break the cycle
                break;
            } catch (IOException e) {
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
        // add content-type header
        request.addHeader(HttpHeaders.CONTENT_TYPE, protocol.getContentType());
        // set headers
        headers.forEach(request::setHeader);
    }



}
