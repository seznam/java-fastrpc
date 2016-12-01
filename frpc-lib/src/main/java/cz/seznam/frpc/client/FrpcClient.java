package cz.seznam.frpc.client;

import cz.seznam.frpc.core.transport.FrpcTransportException;
import cz.seznam.frpc.core.transport.Protocol;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * {@code FrpcClient} provides simple-to-use logic for making {@code FRPC} requests and rather powerful {@code API} for
 * fully type-safe mapping of their responses to Java objects.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcClient.class);

    private HttpClient httpClient;
    private URI uri;
    private Map<String, String> headers;
    private Long connectTimeout;
    private TimeUnit connectTimeoutTimeUnit;
    private Long socketTimeout;
    private TimeUnit socketTimeoutTimeUnit;
    private long retryDelay;
    private TimeUnit retryDelayTimeUnit;
    private int maxAttemptCount;
    private boolean prependImplicitParams = true;
    private List<Object> implicitParameters;
    private Protocol protocol;

    private FrpcClient(HttpClient httpClient, URI uri, Map<String, String> headers, Long connectTimeout,
                       TimeUnit connectTimeoutTimeUnit, Long socketTimeout, TimeUnit socketTimeoutTimeUnit,
                       long retryDelay, TimeUnit retryDelayTimeUnit, int maxAttemptCount,
                       List<Object> implicitParameters, boolean prependImplicitParams, Protocol protocol) {
        this.httpClient = httpClient;
        this.uri = uri;
        this.headers = headers;
        this.connectTimeout = connectTimeout;
        this.connectTimeoutTimeUnit = connectTimeoutTimeUnit;
        this.socketTimeout = socketTimeout;
        this.socketTimeoutTimeUnit = socketTimeoutTimeUnit;
        this.retryDelay = retryDelay;
        this.retryDelayTimeUnit = retryDelayTimeUnit;
        this.maxAttemptCount = maxAttemptCount;
        this.prependImplicitParams = prependImplicitParams;
        this.implicitParameters = implicitParameters;
        this.protocol = protocol;
    }

    /**
     * Builder used to create {@link FrpcClient} instances.
     */
    public static final class Builder {

        private HttpClient httpClient;
        private URI uri;
        private Map<String, String> headers;
        private long connectTimeout = -1;
        private TimeUnit connectTimeoutTimeUnit = TimeUnit.MILLISECONDS;
        private long socketTimeout = -1;
        private TimeUnit socketTimeoutTimeUnit = TimeUnit.MILLISECONDS;
        private long retryDelay;
        private TimeUnit retryDelayTimeUnit = TimeUnit.MILLISECONDS;
        private int maxAttemptCount = 3;
        private boolean prependImplicitParams = true;
        private List<Object> implicitParameters = Collections.emptyList();
        private Protocol protocol;
        private boolean forceProtocolUsage;

        /**
         * Sets {@code URL} to call methods against. This {@code URL} string is converted to {@link URI} which is
         * required by {@link HttpClient} which handles the actual {@code HTTP} transport.
         *
         * @param urlString {@code URL} to be converted to {@code URI} against which methods will be called
         * @return this {@code Builder} instance so that calls can be chained
         * @throws IllegalArgumentException if given {@code URL} string could not be converted to {@code URI}
         */
        public Builder url(String urlString) {
            try {
                this.uri = new URI(Objects.requireNonNull(urlString, "Given URI string must not be null"));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Value " + urlString + " is not a valid URI");
            }
            return this;
        }

        /**
         * Sets {@link URL} to call methods against. This {@code URL} is converted to {@link URI} which is required
         * by {@link HttpClient} which handles the actual {@code HTTP} transport.
         *
         * @param url {@code URL} to be converted to {@code URI} against which methods will be called
         * @return this {@code Builder} instance so that calls can be chained
         * @throws IllegalArgumentException if given {@code URL} could not be converted to {@code URI}
         */
        public Builder url(URL url) {
            try {
                this.uri = Objects.requireNonNull(url, "Given URL must not be null").toURI();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Value " + url + " is not a valid URI");
            }
            return this;
        }

        /**
         * Sets {@link URI} to call methods against. This {@code URI} is required by {@link HttpClient} which handles
         * the actual {@code HTTP} transport.
         *
         * @param uri {@code URI} to call methods against
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder url(URI uri) {
            this.uri = Objects.requireNonNull(uri, "Given URI must not be null");
            return this;
        }

        /**
         * Constructs default {@link HttpClient} and sets it as the client to be used by {@link FrpcClient} being built.
         *
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder usingDefaultHttpClient() {
            this.httpClient = HttpClients.createDefault();
            return this;
        }

        /**
         * Specifies {@link HttpClient} to be used by {@link FrpcClient} being built.
         *
         * @param httpClient the client
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder usingHttpClient(HttpClient httpClient) {
            this.httpClient = Objects.requireNonNull(httpClient, "Given HTTP client must not be null");
            return this;
        }

        /**
         * Specifies default headers to be sent with every request, thus with every {@code FRPC} method call.
         *
         * @param headers headers to send, {@code null} works the same as empty collection
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder defaultHeaders(Map<String, String> headers) {
            if (headers != null) {
                this.headers = new HashMap<>(headers);
            }
            return this;
        }

        /**
         * Determines the timeout in milliseconds until a connection is established.
         * <p>
         * A timeout value of zero is interpreted as an infinite timeout.
         * A negative value is interpreted as undefined (system default).
         * <p>
         * Defaults to -1.
         *
         * @param timeout  timeout value
         * @param timeUnit the time unit
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder connectTimeout(long timeout, TimeUnit timeUnit) {
            this.connectTimeout = timeout;
            this.connectTimeoutTimeUnit = Objects.requireNonNull(timeUnit, "Time unit must not be null");
            return this;
        }

        /**
         * Defines the socket timeout ({@code SO_TIMEOUT}) in given {@link TimeUnit},
         * which is the timeout for waiting for data or, put differently,
         * a maximum period inactivity between two consecutive data packets).
         * <p>
         * A timeout value of zero is interpreted as an infinite timeout.
         * A negative value is interpreted as undefined (system default).
         * <p>
         * Defaults to -1.
         *
         * @param timeout  timeout value
         * @param timeUnit the time unit
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder socketTimeout(long timeout, TimeUnit timeUnit) {
            this.socketTimeout = timeout;
            this.socketTimeoutTimeUnit = Objects.requireNonNull(timeUnit, "Time unit must not be null");
            return this;
        }

        /**
         * Sets number of repeated calls to a method when a call fails.
         * Calls are only repeated in case of transport error (connection timeout etc.).
         *
         * @param attemptCount number of repeated calls to a method when a call fails
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder attemptCount(int attemptCount) {
            this.maxAttemptCount = attemptCount;
            return this;
        }

        /**
         * Sets delay between successive tries to get response from a method (see {@link #attemptCount(int)}).
         * <p>
         * Defaults to zero which means no delay. Negative values are interpreted as no delay as well.
         *
         * @param delay    the delay
         * @param timeUnit the time unit
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder retryDelay(int delay, TimeUnit timeUnit) {
            this.retryDelay = delay;
            this.retryDelayTimeUnit = Objects.requireNonNull(timeUnit, "Time unit must not be null");
            return this;
        }

        /**
         * Sets implicit parameters. Given array is copied so the caller is free to modify afterwards without any
         * effect on this {@code Builder} instance. <br />
         * Items in array are automatically added into each request before regular
         * FRPC method parameters. This can be useful when sending info about device,
         * user, etc. with each request.
         *
         * @param implicitParameters array of implicit parameters
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder implicitParameters(Object... implicitParameters) {
            return implicitParameters(true, implicitParameters);
        }

        /**
         * Sets implicit parameters. Given array is copied so the caller is free to modify afterwards without any
         * effect on this {@code Builder} instance. <br />
         * Items in array are automatically added into each request before regular
         * FRPC method parameters. This can be useful when sending info about device,
         * user, etc. with each request.
         *
         * @param prepend            if {@code true} then implicit parameters will be serialized <strong>before</strong> regular
         *                           method parameters; if {@code false} then implicit parameters will be serialized
         *                           <strong>after</strong> regular method parameters
         * @param implicitParameters array of implicit parameters
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder implicitParameters(boolean prepend, Object... implicitParameters) {
            Objects.requireNonNull(implicitParameters);
            this.prependImplicitParams = prepend;
            this.implicitParameters = stream(implicitParameters).collect(Collectors.toList());
            return this;
        }

        /**
         * Convenience method for calling {@link #protocol(Protocol, boolean)} with {@code false} as the second
         * parameter.
         *
         * @param protocol protocol to use when communicating with server
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder protocol(Protocol protocol) {
            return protocol(protocol, false);
        }

        /**
         * Specifies protocol to use when communicating with host. This is not necessary as the {@code Builder}
         * automatically negotiates suitable protocol to use on invocation of {@link #build()} method. If the protocol
         * is specified and the server doesn't support it, an exception is thrown by default. <br />
         * To overcome this and connect to server which doesn't claim to support given protocol anyway,
         * {@code forceUsage} parameter can be set to {@code true} in which case the client ignores the fact that server
         * doesn't seem to support given protocol.
         *
         * @param protocol   protocol to use when communicating with server
         * @param forceUsage if {@code true} then no exception is thrown even if the server doesn't claim to support
         *                   given {@code protocol}
         * @return this {@code Builder} instance so that calls can be chained
         */
        public Builder protocol(Protocol protocol, boolean forceUsage) {
            this.forceProtocolUsage = forceUsage;
            this.protocol = Objects.requireNonNull(protocol, "Protocol must not be null");
            return this;
        }

        /**
         * Builds a {@link FrpcClient} using properties set on this {@code Builder}. This method first tries to
         * negotiate protocol to be used. Then it constructs new instance of {@code FrpcClient} using proper protocol
         * and properties from this builder.
         *
         * @return new instance of {@code FrpcClient} built from properties set on this builder
         * @throws FrpcTransportException if the protocol specified cannot be used with server on given address
         * @see #protocol(Protocol, boolean)
         */
        public FrpcClient build() {
            // if no HttpClient was specified, use the default one
            if (httpClient == null) {
                this.usingDefaultHttpClient();
                LOGGER.info("No HttpClient specified, using default HttpClient to handle HTTP transport");
            }

            try {
                // try to discover protocols supported by the server
                Set<Protocol> serverSupportedProtocols = discoverSupportedProtocols();
                // if protocol to use was specified beforehand
                if (protocol != null) {
                    // check that server supports that protocol
                    if (!serverSupportedProtocols.contains(protocol)) {
                        String errorMessage = String
                                .format("Protocol to use was set to %s which is NOT supported by the" +
                                                " server. Supported protocols are %s.", protocol.name(),
                                        serverSupportedProtocols);
                        // if it doesn't, refuse to use that protocol if we are not forced to do so
                        if (forceProtocolUsage) {
                            // ok, we will use it anyway, but it's not quite right
                            LOGGER.warn(errorMessage);
                        } else {
                            // nope, this protocol is unsupported, we won't use it
                            throw new FrpcTransportException(errorMessage);
                        }
                    } else {
                        // if the protocol is supported, everything is OK
                        LOGGER.debug("Protocol to use was set to {} which is supported by the server.",
                                protocol.name());
                    }
                } else {
                    // if no protocol to use was specified beforehand, check if there is at least one protocol supported by
                    // the server
                    if (serverSupportedProtocols.isEmpty()) {
                        // if there is none, throw an exception
                        throw new FrpcTransportException("No protocol to use was explicitly specified, yet the" +
                                " server does not claim to support any compatible protocol either. To use " +
                                FrpcClient.class
                                        .getSimpleName() + " with this server, set protocol explicitly and force" +
                                " its usage.");
                    } else {
                        // there is one or more protocols supported by the server, pick the most preferred one
                        this.protocol = serverSupportedProtocols.iterator().next();
                        if (serverSupportedProtocols.size() == 1) {
                            LOGGER.debug("Server supports {} protocol, will use that.", protocol);
                        } else {
                            LOGGER.debug("Server supports following protocols: {}. Will use {}.",
                                    serverSupportedProtocols,
                                    protocol);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error while trying to discover protocols supported by the server", e);
                throw new FrpcTransportException("Error while trying to discover protocols supported by the server", e);
            }

            // build the client
            return new FrpcClient(httpClient, uri, headers, connectTimeout, connectTimeoutTimeUnit, socketTimeout,
                    socketTimeoutTimeUnit, retryDelay, retryDelayTimeUnit, maxAttemptCount, implicitParameters,
                    prependImplicitParams, protocol);
        }

        private Set<Protocol> discoverSupportedProtocols() throws IOException {
            // try to do a HEAD request to given URL
            HttpHead head = new HttpHead(uri);
            head.addHeader(HttpHeaders.ACCEPT, "text/xml, application/x-frpc");
            HttpResponse response = httpClient.execute(head);
            // get all "Accept" header values as Set of strings
            Set<String> acceptHeaderValues = Arrays.stream(response.getHeaders(HttpHeaders.ACCEPT))
                    .map(Header::getElements).flatMap(Arrays::stream).map(HeaderElement::getName)
                    .collect(Collectors.toSet());
            // check if they contain "application/x-frpc" or "text/xml" or both
            Set<Protocol> result = EnumSet.noneOf(Protocol.class);
            if (acceptHeaderValues.contains(Protocol.FRPC.getContentType())) {
                result.add(Protocol.FRPC);
            }
            if (acceptHeaderValues.contains(Protocol.XML_RPC.getContentType())) {
                result.add(Protocol.XML_RPC);
            }
            // return the result as set of protocols supported by the server
            return result;
        }

    }

    /**
     * Creates new {@link Builder} instance.
     *
     * @return new {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns underlying {@code HttpClient} used to handler {@code HTTP} transport.
     *
     * @return underlying {@code HttpClient} used to handler {@code HTTP} transport
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Returns {@code URI} to which requests are sent when calling {@code FRPC} methods.
     *
     * @return {@code URI} to which requests are sent when calling {@code FRPC} methods
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns connection timeout in given {@link TimeUnit}.
     *
     * @param timeUnit time unit to convert the value to
     * @return connection timeout in given {@link TimeUnit}
     * @see Builder#connectTimeout(long, TimeUnit)
     */
    public Long getConnectTimeout(TimeUnit timeUnit) {
        return Objects.requireNonNull(timeUnit, "Time unit cannot be null")
                .convert(connectTimeout, connectTimeoutTimeUnit);
    }

    /**
     * Returns socket timeout in given {@link TimeUnit}.
     *
     * @param timeUnit time unit to convert the value to
     * @return socket timeout in given {@link TimeUnit}
     * @see Builder#socketTimeout(long, TimeUnit)
     */
    public Long getSocketTimeout(TimeUnit timeUnit) {
        return Objects.requireNonNull(timeUnit, "Time unit cannot be null")
                .convert(socketTimeout, socketTimeoutTimeUnit);
    }

    /**
     * Returns retry delay in given {@link TimeUnit}.
     *
     * @param timeUnit time unit to convert the value to
     * @return retry delay in given {@link TimeUnit}
     * @see Builder#retryDelay(int, TimeUnit)
     */
    public long getRetryDelay(TimeUnit timeUnit) {
        return Objects.requireNonNull(timeUnit, "Time unit cannot be null")
                .convert(retryDelay, retryDelayTimeUnit);
    }

    /**
     * Returns number of repeated calls to a method when a call fails.
     *
     * @return number of repeated calls to a method when a call fails
     * @see Builder#attemptCount(int)
     */
    public int getMaxAttemptCount() {
        return maxAttemptCount;
    }

    /**
     * Returns the {@link Protocol} used by this {@code FrpcClient} to communicate with the server. The protocol is
     * either the protocol specified using the {@link Builder} or protocol negotiated with the server by the
     * {@code Builder} when creating this {@code FrpcClient} instance.
     *
     * @return the {@link Protocol} used by this {@code FrpcClient} to communicate with the server
     * @see Builder#protocol(Protocol)
     * @see Builder#protocol(Protocol, boolean)
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * Returns an unmodifiable view of a map representing default headers sent witch every request to the server.
     *
     * @return an unmodifiable view of a map representing default headers sent witch every request to the server
     */
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Returns number of repeated calls to a method when a call fails.
     * <p>
     * Defaults to 3.
     *
     * @return number of repeated calls to a method when a call fails
     */
    public int getAttemptCount() {
        return maxAttemptCount;
    }

    /**
     * Returns an immutable view of currently set implicit parameters.
     *
     * @return immutable list of implicit parameters
     */
    public List<Object> getImplicitParameters() {
        return Collections.unmodifiableList(implicitParameters);
    }

    /**
     * Prepares call to method of given name with given parameters and returns it without executing. This can be useful
     * for one-time overrides of properties set to this {@code FrpcClient}.
     *
     * @param method name of the {@code FRPC} method to call
     * @param params {@code FRPC} method params
     * @return instance of {@link FrpcMethodCall} representing the method call which has not yet been executed
     */
    public FrpcMethodCall prepareCall(String method, Object... params) {
        // check arguments
        Objects.requireNonNull(method);
        List<Object> paramsAsList = Arrays.asList(Objects.requireNonNull(params));
        // and create FrpcMethodCall object
        return new FrpcMethodCall(httpClient, uri, protocol, implicitParameters, prependImplicitParams, headers,
                maxAttemptCount, retryDelay, retryDelayTimeUnit, method, paramsAsList);
    }

    /**
     * Prepares call to method of given name with given parameters and executes it right away. The result of remote
     * method is then returned.
     *
     * @param method name of the {@code FRPC} method to call
     * @param params {@code FRPC} method params
     * @return result of remote method invocation wrapped in {@code FrpcCallResult}
     */
    public FrpcCallResult<Object> call(String method, Object... params) {
        return prepareCall(method, params).getResult();
    }

}
