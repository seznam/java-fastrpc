package cz.sklik.frpc;

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

    private static final String FRPC_HTTP_URL      = "http://%s:%d%s";

    private static final String FRPC_HTTPS_URL     = "https://%s:%d%s";

    int                         mConnectionTimeout = 10000;

    int                         mReadTimeout       = 10000;

    int                         mWriteTimeout      = 10000;

    String                      mHost              = "";

    int                         mPort              = 0;

    String                      mPath              = "";

    int                         mAttempts          = 3;

    boolean                     mKeepAlive         = true;

    boolean                     mUseChunkedData    = false;

    Object[]                    mParamPrefix       = {};

    int                         mBufferSize        = 8192;

    boolean                     mUseSSL            = false;

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
        mHost = host;
        mPort = port;
        mPath = path;
    }

    /**
     * Creates fprc config from given config.
     * 
     * @param config
     */
    public FrpcConfig(FrpcConfig config) {
        this.mConnectionTimeout = config.mConnectionTimeout;
        this.mReadTimeout = config.mReadTimeout;
        this.mWriteTimeout = config.mWriteTimeout;
        this.mHost = config.mHost;
        this.mPort = config.mPort;
        this.mPath = config.mPath;
        this.mAttempts = config.mAttempts;
        this.mKeepAlive = config.mKeepAlive;
        this.mUseChunkedData = config.mUseChunkedData;
        this.mParamPrefix = config.mParamPrefix;
        this.mBufferSize = config.mBufferSize;
    }

    /**
     * Returns connection timeout in millis.
     * 
     * Default is 10000.
     * 
     * @return connection timeout in millis
     * 
     * @see getConnectionTimeout
     */
    public int getConnectionTimeout() {
        return mConnectionTimeout;
    }

    /**
     * Sets connections timeout in millis.
     * 
     * @param timeout
     * 
     * @see getConnectionTimeout
     */
    public void setConnectionTimeout(int timeout) {
        mConnectionTimeout = timeout;
    }

    /**
     * Returns read timeout in millis.
     * 
     * Default is 10000.
     * 
     * @return read timeout in millis
     * 
     * @see setReadTimeout
     */
    public int getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * Serts read timeout in millis.
     * 
     * @param timeout
     * 
     * @see getReadTimeout
     */
    public void setReadTimeout(int timeout) {
        mReadTimeout = timeout;
    }

    /**
     * Returns frpc host.
     * 
     * @return frpc host
     * 
     * @see setHost
     */
    public String getHost() {
        return mHost;
    }

    /**
     * Sets frpc host.
     * 
     * @param host
     * 
     * @see getHost
     */
    public void setHost(String host) {
        mHost = host;
    }

    /**
     * Returns frpc server port;
     * 
     * @return frpc server port
     * 
     * @see setPort
     */
    public int getPort() {
        return mPort;
    }

    /**
     * Sets frpc server port.
     * 
     * @param port
     * 
     * @see setPort
     */
    public void setPort(int port) {
        mPort = port;
    }

    /**
     * Returns frpc path.
     * 
     * @return frpc path
     * 
     * @see setPath
     */
    public String getPath() {
        return mPath;
    }

    /**
     * Sets frpc path.
     * 
     * @param path
     * 
     * @see getPath
     */
    public void setPath(String path) {
        mPath = path;
    }

    /**
     * Returns max count of request attempt in case of fault.
     * 
     * Default is 3.
     * 
     * @return max count of request attempt
     * 
     * @see setAttemptCount
     */
    public int getAttemptCount() {
        return mAttempts;
    }

    /**
     * Sets max count of request attempt in case of fault.
     * 
     * Requests are repeated in case of connection error.
     * 
     * @param count
     * 
     * @see getAttemptCount
     */
    public void setAttemptCount(int count) {
        mAttempts = count;
    }

    /**
     * Returns if keep alive is enabled.
     * 
     * Default is true
     * 
     * @return true if keep alive is enabled, otherwise false
     * 
     * @see setKeepAlive
     */
    public boolean isKeepAlive() {
        return mKeepAlive;
    }

    /**
     * Sets keep alive.
     * 
     * @param keepAlive
     * 
     * @see isKeepAlive
     */
    public void setKeepAlive(boolean keepAlive) {
        mKeepAlive = keepAlive;
    }

    /**
     * Returns if use chunked data is enabled.
     * 
     * Default is false.
     * 
     * @return true, if use chunked data is enabled
     * 
     * @see setUseChunkedData
     */
    public boolean useChunkedData() {
        return mUseChunkedData;
    }

    /**
     * Enables use chunked data
     * 
     * @param useChunkedData
     * 
     * @see useChunkedData
     */
    public void setUseChunkedData(boolean useChunkedData) {
        mUseChunkedData = useChunkedData;
    }

    /**
     * Returns array of parametr prefix.
     * 
     * 
     * @return parametr prefix
     * 
     * @see setParametrPrefix
     */
    public Object[] getParametrPrefix() {
        return mParamPrefix;
    }

    /**
     * Sets array of parametr prefix.
     * 
     * Items in array are automatically added into each request before regular
     * frpc method parameters.
     * For example this could be useful if you are sending info about device,
     * user, etc. with each request.
     * 
     * @param parametr prefix
     * 
     * @see getPrametrPrefix
     */
    public void setParametrPrefix(Object[] prefix) {
        mParamPrefix = prefix;
    }

    public void setUseSSL(boolean useSSL) {
        mUseSSL = useSSL;
    }

    public boolean useSSL() {
        return mUseSSL;
    }

    /**
     * Builds frpc server url from given configuration.
     * 
     * @param frpc configuration
     * 
     * @return frpc server address if config is ok, otherwise false
     */
    public static URL buildUrl(FrpcConfig config) {
        URL url = null;
        if (config != null) {
            String format = config.useSSL() ? FRPC_HTTPS_URL : FRPC_HTTP_URL;

            String path = String.format(format, config.mHost, config.mPort, config.mPath);
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
