package cz.seznam.frpc.client;

import cz.seznam.frpc.core.transport.*;
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
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Represents a {@code FRPC} method call ready to be executed. Instances are created by {@link FrpcClient} using its
 * current setting, yet several properties (like request timeouts for example) can be overridden using methods of this
 * class.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcMethodCall {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcMethodCall.class);

    private HttpClient client;
    private URI uri;
    private Protocol protocol;
    private List<Object> implicitParameters;
    private boolean prependImplicitParams;
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

    FrpcMethodCall(HttpClient client, URI uri, Protocol protocol, List<Object> implicitParameters,
                   boolean prependImplicitParams, Map<String, String> headers, int maxAttemptCount, long retryDelay,
                   TimeUnit retryDelayTimeUnit, String method, List<Object> parameters) {
        this.client = client;
        this.uri = uri;
        this.protocol = protocol;
        this.implicitParameters = implicitParameters == null ? Collections.emptyList() : implicitParameters;
        this.prependImplicitParams = prependImplicitParams;
        this.headers = headers == null ? Collections.emptyMap() : headers;
        this.maxAttemptCount = maxAttemptCount;
        this.retryDelay = retryDelay;
        this.retryDelayTimeUnit = retryDelayTimeUnit;
        this.method = method;
        this.parameters = parameters;
    }

    /**
     * Overrides implicit parameters set by the {@link FrpcClient} by given value.
     *
     * @param implicitParameters implicit parameters override
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     */
    public FrpcMethodCall withImplicitParameters(Object... implicitParameters) {
        this.implicitParameters = implicitParameters == null ? null :
                Arrays.stream(implicitParameters).collect(Collectors.toList());
        return this;
    }

    /**
     * Adds given parameters to the array of implicit parameters set by the {@link FrpcClient}.
     *
     * @param implicitParameters implicit parameters to add to those set by the {@link FrpcClient}
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     */
    public FrpcMethodCall withAddedImplicitParameters(Object... implicitParameters) {
        if (ArrayUtils.isNotEmpty(implicitParameters)) {
            Arrays.stream(implicitParameters).forEach(this.implicitParameters::add);
        }
        return this;
    }

    /**
     * Configures this {@code FrpcMethodCall} to <i>prepend</i> implicit parameters before regular method parameters.
     *
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     */
    public FrpcMethodCall withImplicitParametersPrepended() {
        this.prependImplicitParams = true;
        return this;
    }

    /**
     * Configures this {@code FrpcMethodCall} to <i>append</i> implicit parameters after regular method parameters.
     *
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     */
    public FrpcMethodCall withImplicitParametersAppended() {
        this.prependImplicitParams = false;
        return this;
    }

    /**
     * Overrides request headers set by the {@link FrpcClient} by given value.
     *
     * @param headers request headers override
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     */
    public FrpcMethodCall withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Adds given name-value pair to request headers to be send with the request.
     * <p>
     * Note that headers are stored in a {@link Map} by their names, so if there already is a header of the same name
     * as the one currently being added, then its value will be <i>overwritten</i> by the new value.
     *
     * @param name name of the header
     * @param value value od the header
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     */
    public FrpcMethodCall withNewHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Adds given collection of name-value pairs to request headers to be send with the request.
     * <p>
     * Note that headers are also stored in a {@link Map} by their names, so if there already is a header of the same name
     * as one of those currently being added, then its value will be <i>overwritten</i> by the new value.
     *
     * @param headers headers to add to the request
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     */
    public FrpcMethodCall withNewHeaders(Map<String, String> headers) {
        this.headers.putAll(Objects.requireNonNull(headers));
        return this;
    }

    /**
     * Overrides "connect timeout" set by the {@link FrpcClient} by given value.
     *
     * @param newTimeout new timeout value
     * @param timeUnit time unit of given value
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     * @see FrpcClient.Builder#connectTimeout(long, TimeUnit)
     */
    public FrpcMethodCall withConnectTimeout(long newTimeout, TimeUnit timeUnit) {
        this.connectTimeout = newTimeout;
        this.connectTimeoutTimeUnit = Objects.requireNonNull(timeUnit);
        return this;
    }

    /**
     * Overrides "socket timeout" set by the {@link FrpcClient} by given value.
     *
     * @param newTimeout new timeout value
     * @param timeUnit time unit of given value
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     * @see FrpcClient.Builder#socketTimeout(long, TimeUnit)
     */
    public FrpcMethodCall withSocketTimeout(long newTimeout, TimeUnit timeUnit) {
        this.socketTimeout = newTimeout;
        this.socketTimeoutTimeUnit = Objects.requireNonNull(timeUnit);
        return this;
    }

    /**
     * Overrides "attempt count" set by the {@link FrpcClient} by given value.
     *
     * @param newAttemptCount new attempt count value
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     * @see FrpcClient.Builder#attemptCount(int)
     */
    public FrpcMethodCall withAttemptCount(int newAttemptCount) {
        this.maxAttemptCount = newAttemptCount;
        return this;
    }

    /**
     * Overrides "retry delay" set by the {@link FrpcClient} by given value.
     *
     * @param newRetryDelay new retry delay value
     * @param timeUnit time unit of given value
     * @return this {@code FrpcMethodCall} instance so that setters can be chained
     * @see FrpcClient.Builder#retryDelay(int, TimeUnit)
     */
    public FrpcMethodCall withRetryDelay(int newRetryDelay, TimeUnit timeUnit) {
        this.retryDelay = newRetryDelay;
        this.retryDelayTimeUnit = Objects.requireNonNull(timeUnit);
        return this;
    }

    /**
     * Invokes the remote method and returns its result wrapped in a {@link FrpcCallResult}.
     *
     * @return the result of remote method invocation
     */
    public FrpcCallResult<Object> getResult() {
        return doRemoteInvocation();
    }

    private FrpcCallResult<Object> doRemoteInvocation() {
        int attempts = 0;
        FrpcCallResult<Object> output = null;

        do {
            attempts++;
            try {
                // get FrpcRequestWriter for current protocol
                FrpcRequestWriter requestWriter = FrpcRequestWriter.forProtocol(protocol);
                // create FrpcRequest
                FrpcRequest frpcRequest = new FrpcRequest(method, prepareMethodParameters());
                // write it
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                requestWriter.write(frpcRequest, baos);

                // prepare the request
                HttpPost request = prepareRequest();
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
                    // create FRPC result out of the unmarshalled response
                    output = new FrpcCallResult<>(responseObject, response.getStatusLine().getStatusCode());
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
                // done, break the cycle
                break;
            } catch (IOException e) {
                if (attempts == maxAttemptCount) {
                    throw new FrpcTransportException(
                            "An error occurred repeatedly (" + maxAttemptCount + " times) while trying to call FRPC method " + method,
                            e);
                }
                if (retryDelay > 0) {
                    try {
                        Thread.sleep(retryDelayTimeUnit.toMillis(retryDelay));
                    } catch (InterruptedException e1) {
                        LOGGER.warn("An interrupted exception occurred while waiting for {} {} before another try.",
                                retryDelay, retryDelayTimeUnit.name());
                    }
                }
            }
        } while (attempts <= maxAttemptCount);
        // return the result
        return output;
    }

    private HttpPost prepareRequest() {
        HttpPost request = new HttpPost(uri);
        // set timeouts
        if (connectTimeout != null || socketTimeout != null) {
            RequestConfig.Builder configBuilder = RequestConfig.custom();
            if (connectTimeout != null) {
                configBuilder.setConnectTimeout((int) connectTimeoutTimeUnit.toMillis(connectTimeout));
            }
            if (socketTimeout != null) {
                configBuilder.setSocketTimeout((int) socketTimeoutTimeUnit.toMillis(socketTimeout));
            }
            request.setConfig(configBuilder.build());
        }
        // add content-type header
        request.addHeader(HttpHeaders.CONTENT_TYPE, protocol.getContentType());
        // set headers
        headers.forEach(request::setHeader);
        // return the request
        return request;
    }

    private List<Object> prepareMethodParameters() {
        // if no implicit parameters are given
        if (implicitParameters == null || implicitParameters.isEmpty()) {
            // return just regular method parameters
            return parameters;
        } else {
            // if there are implicit parameters but no regular parameters
            if (parameters == null || parameters.isEmpty()) {
                // return just implicit ones
                return implicitParameters;
            } else {
                // if there is both, prepend or append implicit parameters
                List<Object> params = new ArrayList<>();
                if (prependImplicitParams) {
                    params.addAll(implicitParameters);
                }
                params.addAll(parameters);
                if (!prependImplicitParams) {
                    params.addAll(implicitParameters);
                }
                // and return just one list of all parameters
                return params;
            }
        }
    }

}
