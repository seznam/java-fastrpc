package cz.seznam.frpc.client;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class holding Frpc connection settings.
 * 
 * User has to set host and port at least.
 * 
 * @author Jakub Janda
 * 
 */
public class FrpcConfig {

    private static final String FRPC_HTTP_URL = "http://%s:%d%s/";

    private static final String FRPC_HTTPS_URL = "https://%s:%d%s/";

    private int connectionTimeout = 10000;

    private int readTimeout = 10000;

    private int writeTimeout = 10000;

    private String host = "";

    private int port = 0;

    private String path = "";

    private int attempts = 3;

    private boolean keepAlive = true;

    private boolean useChunkedData = false;

    private Object[] paramPrefix = {};

    private int bufferSize = 8192;

    private boolean useSSL = false;

    /**
     * Created empty frpc config.
     */
    public FrpcConfig() {
    }

    /**
     * Creates frpc config with given server params.
     * 
     * @param host
     * @param port
     * @param path
     */
    public FrpcConfig(String host, int port, String path) {
        this.host = host;
        this.port = port;
        this.path = path;
    }

    /**
     * Creates fprc config from given config.
     * 
     * @param config
     */
    public FrpcConfig(FrpcConfig config) {
        this.connectionTimeout = config.connectionTimeout;
        this.readTimeout = config.readTimeout;
        this.writeTimeout = config.writeTimeout;
        this.host = config.host;
        this.port = config.port;
        this.path = config.path;
        this.attempts = config.attempts;
        this.keepAlive = config.keepAlive;
        this.useChunkedData = config.useChunkedData;
        this.paramPrefix = config.paramPrefix;
        this.bufferSize = config.bufferSize;
    }

    /**
     * Returns connection timeout in millis.
     * 
     * Default is 10000.
     * 
     * @return connection timeout in millis
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets connections timeout in millis.
     * 
     * @param timeout
     */
    public void setConnectionTimeout(int timeout) {
        connectionTimeout = timeout;
    }

    /**
     * Returns read timeout in millis.
     * 
     * Default is 10000.
     * 
     * @return read timeout in millis
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Serts read timeout in millis.
     * 
     * @param timeout
     */
    public void setReadTimeout(int timeout) {
        readTimeout = timeout;
    }

    /**
     * Returns frpc host.
     * 
     * @return frpc host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets frpc host.
     * 
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns frpc server port;
     * 
     * @return frpc server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets frpc server port.
     * 
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns frpc path.
     * 
     * @return frpc path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets frpc path.
     * 
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns max count of request attempt in case of fault.
     * 
     * Default is 3.
     * 
     * @return max count of request attempt
     */
    public int getAttemptCount() {
        return attempts;
    }

    /**
     * Sets max count of request attempt in case of fault.
     * 
     * Requests are repeated in case of connection error.
     * 
     * @param count
     */
    public void setAttemptCount(int count) {
        attempts = count;
    }

    /**
     * Returns if keep alive is enabled.
     * 
     * Default is true
     * 
     * @return true if keep alive is enabled, otherwise false
     */
    public boolean isKeepAlive() {
        return keepAlive;
    }

    /**
     * Sets keep alive.
     * 
     * @param keepAlive
     */
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * Returns if use chunked data is enabled.
     * 
     * Default is false.
     * 
     * @return true, if use chunked data is enabled
     */
    public boolean useChunkedData() {
        return useChunkedData;
    }

    /**
     * Enables use chunked data
     * 
     * @param useChunkedData
     */
    public void setUseChunkedData(boolean useChunkedData) {
        this.useChunkedData = useChunkedData;
    }

    /**
     * Returns array of parametr prefix.
     * 
     * 
     * @return parametr prefix
     */
    public Object[] getParametrPrefix() {
        return paramPrefix;
    }

    /**
     * Sets array of parametr prefix.
     * 
     * Items in array are automatically added into each request before regular
     * frpc method parameters.
     * For example this could be useful if you are sending info about device,
     * user, etc. with each request.
     * 
     * @param prefix
     */
    public void setParametrPrefix(Object[] prefix) {
        paramPrefix = prefix;
    }

    /**
     * Returns the buffer size, defaults to 8192.
     *
     * @return buffer size
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the buffer size.
     *
     * @param bufferSize buffer size to set to this config
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public boolean useSSL() {
        return useSSL;
    }

    /**
     * Builds frpc server url from given configuration.
     * 
     * @param config configuration
     * 
     * @return frpc server address if config is ok, otherwise false
     */
    public static URL buildUrl(FrpcConfig config) {
        URL url = null;
        if (config != null) {
            String format = config.useSSL() ? FRPC_HTTPS_URL : FRPC_HTTP_URL;

            String path = String.format(format, config.host, config.port, config.path);
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                url = null;
            }
        } else {
            url = null;
        }
        return url;
    }
}
