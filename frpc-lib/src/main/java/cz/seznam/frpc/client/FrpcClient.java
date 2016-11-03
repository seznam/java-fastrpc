package cz.seznam.frpc.client;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcClient {

    private HttpClient httpClient;
    private String urlString;
    private Map<String, String> headers;
    private long retryDelay = 0;
    private TimeUnit retryDelayTimeUnit = TimeUnit.MILLISECONDS;
    private int maxAttemptCount = 3;
    private List<Object> implicitParameters = Collections.emptyList();

    public FrpcClient(String urlString) {
        this(urlString, HttpClients.createDefault());
    }

    public FrpcClient(URL url) {
        this(url, HttpClients.createDefault());
    }

    public FrpcClient(String urlString, HttpClient httpClient) {
        this.urlString = FrpcClientUtils.createURL(urlString).toString();
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    public FrpcClient(URL url, HttpClient httpClient) {
        this.urlString = Objects.requireNonNull(url).toString();
        this.httpClient = Objects.requireNonNull(httpClient);
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
        this.implicitParameters = Arrays.stream(implicitParameters).collect(Collectors.toList());
    }

    public FrpcMethodCall prepareCall(String method, Object... params) {
        // check arguments
        Objects.requireNonNull(method);
        List<Object> paramsAsList = Arrays.asList(Objects.requireNonNull(params));
        // and create FrpcMethodCall object
        return new FrpcMethodCall(httpClient, new HttpPost(urlString), implicitParameters, headers, maxAttemptCount,
                retryDelay, retryDelayTimeUnit, method, paramsAsList);
    }

    public FrpcCallResult call(String method, Object... params) {
        return prepareCall(method, params).getResult();
    }

    public UnwrappedFrpcCallResult callAndUnwrap(String method, Object... params) {
        return prepareCall(method, params).getResult().unwrap();
    }

}
