package cz.seznam.frpc.client;

import cz.seznam.frpc.core.transport.FrpcTransportException;
import cz.seznam.frpc.core.transport.Protocol;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcClient.class);

    private HttpClient httpClient;
    private String urlString;
    private Map<String, String> headers;
    private long retryDelay = 0;
    private TimeUnit retryDelayTimeUnit = TimeUnit.MILLISECONDS;
    private int maxAttemptCount = 3;
    private List<Object> implicitParameters = Collections.emptyList();
    private Protocol protocol;
    private boolean forceProtocolUsage;

    public FrpcClient(String urlString) {
        this(urlString, HttpClients.createDefault());
    }

    public FrpcClient(URL url) {
        this(url, HttpClients.createDefault());
    }

    public FrpcClient(String urlString, HttpClient httpClient) {
        this.urlString = FrpcClientUtils.createURL(urlString).toString();
        this.httpClient = Objects.requireNonNull(httpClient);
        init();
    }

    public FrpcClient(URL url, HttpClient httpClient) {
        this.urlString = Objects.requireNonNull(url).toString();
        this.httpClient = Objects.requireNonNull(httpClient);
        init();
    }

    private void init() {
        try {
            // try to discover protocols supported by the server
            Set<Protocol> serverSupportedProtocols = discoverSupportedProtocols();
            // if protocol to use was specified beforehand
            if(protocol != null) {
                // check that server supports that protocol
                if(!serverSupportedProtocols.contains(protocol)) {
                    String errorMessage = String.format("Protocol to use was set to %s which is NOT supported by the" +
                            " server. Supported protocols are %s.", protocol.name(), serverSupportedProtocols);
                    // if it doesn't, refuse to use that protocol if we are not forced to do so
                    if(forceProtocolUsage) {
                        // ok, we will use it anyway, but it's not quite right
                        LOGGER.warn(errorMessage);
                    } else {
                        // nope, this protocol is unsupported, we won't use it
                        throw new FrpcTransportException(errorMessage);
                    }
                } else {
                    // if the protocol is supported, everything is OK
                    LOGGER.debug("Protocol to use was set to {} which is supported by the server.", protocol.name());
                }
            } else {
                // if no protocol to use was specified beforehand, check if there is at least one protocol supported by
                // the server
                if(serverSupportedProtocols.isEmpty()) {
                    // if there is none, throw an exception
                    throw new FrpcTransportException("No protocol to use was explicitly specified, yet the" +
                            " server does not claim to support any compatible protocol either. To use " +
                            FrpcClient.class.getSimpleName() + " with this server, set protocol explicitly and force" +
                            " its usage.");
                } else {
                    // there is one or more protocols supported by the server, pick the most preferred one
                    this.protocol = serverSupportedProtocols.iterator().next();
                    if(serverSupportedProtocols.size() == 1) {
                        LOGGER.debug("Server supports {} protocol, will use that.", protocol);
                    } else  {
                        LOGGER.debug("Server supports following protocols: {}. Will use {}.", serverSupportedProtocols,
                                protocol);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error while trying to discover protocols supported by the server", e);
            throw new FrpcTransportException("Error while trying to discover protocols supported by the server", e);
        }
    }

    private Set<Protocol> discoverSupportedProtocols() throws IOException {
        // try to do a HEAD request to given URL
        HttpHead head = new HttpHead(urlString);
        head.addHeader(HttpHeaders.ACCEPT, "text/xml, application/x-frpc");
        HttpResponse response = httpClient.execute(head);
        // get all "Accept" header values as Set of strings
        Set<String> acceptHeaderValues = Arrays.stream(response.getHeaders(HttpHeaders.ACCEPT))
                .map(Header::getElements).flatMap(Arrays::stream).map(HeaderElement::getName)
                .collect(Collectors.toSet());
        // check if they contain "application/x-frpc" or "text/xml" or both
        Set<Protocol> result = EnumSet.noneOf(Protocol.class);
        if(acceptHeaderValues.contains(Protocol.FRPC.getContentType())) {
            result.add(Protocol.FRPC);
        }
        if(acceptHeaderValues.contains(Protocol.XML_RPC.getContentType())) {
            result.add(Protocol.XML_RPC);
        }
        // return the result as set of protocols supported by the server
        return result;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Returns number of repeated calls to a method when a call fails.
     *
     * Default is 3.
     *
     * @return number of repeated calls to a method when a call fails
     */
    public int getAttemptCount() {
        return maxAttemptCount;
    }

    /**
     * Sets number of repeated calls to a method when a call fails.
     * Calls are only repeated in case of transport error (connection timeout etc.).
     *
     * @param count number of repeated calls to a method when a call fails
     */
    public void setAttemptCount(int count) {
        maxAttemptCount = count;
    }

    /**
     * Returns an immutable view of currently set implicit parameters.
     *
     * @return immutable list of implicit parameters
     * @see #setImplicitParameters(Object[])
     */
    public List<Object> getImplicitParameters() {
        return Collections.unmodifiableList(implicitParameters);
    }

    /**
     * Sets implicit parameters. Given array is copied so the caller is free to modify afterwards without any
     * effect on this {@code FrpcClient} instance. <br />
     * Items in array are automatically added into each request before regular
     * FRPC method parameters. This can be useful when sending info about device,
     * user, etc. with each request.
     *
     * @param implicitParameters array of implicit parameters
     */
    public void setImplicitParameters(Object... implicitParameters) {
        Objects.requireNonNull(implicitParameters);
        this.implicitParameters = stream(implicitParameters).collect(Collectors.toList());
    }

    public FrpcMethodCall prepareCall(String method, Object... params) {
        // check arguments
        Objects.requireNonNull(method);
        List<Object> paramsAsList = Arrays.asList(Objects.requireNonNull(params));
        // and create FrpcMethodCall object
        return new FrpcMethodCall(httpClient, new HttpPost(urlString), protocol, implicitParameters, headers,
                maxAttemptCount, retryDelay, retryDelayTimeUnit, method, paramsAsList);
    }

    public FrpcCallResult call(String method, Object... params) {
        return prepareCall(method, params).getResult();
    }

    public UnwrappedFrpcCallResult callAndUnwrap(String method, Object... params) {
        return prepareCall(method, params).getResult().unwrap();
    }

}
