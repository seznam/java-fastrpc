package cz.seznam.frpc.client;

import cz.seznam.frpc.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Frpc client class.
 * 
 * With this class you can call fprc methods with parametrs at server with given
 * configuration.
 * 
 * Class contains call methods with different types of result data for easy use
 * in different cases.
 * 
 * All calls of this class are synchronous. For easy asynchronous calls use
 * FrpcAsynTask,
 * or implement your own threads using FrpcClient.
 * 
 * FrpcTypes mapping:
 * 
 * 
 * This frpc library uses java primitive types (or their wrapper classes) as frpc types.
 * Frpc structure is used as Map<String, Object>, frpc array as Object[].
 * Datetime is used as GregorianCalendar.
 * 
 * @author Jakub Janda
 * 
 */
public class FrpcClient {

    static String LOGTAG = "FRPC";

    static String LOGRESULT = "FRPCRESULT";

    private FrpcConfig configuration;

    private HttpURLConnection connection;

    private URL url;

    private Map<String, String> requestCookies;

    private Map<String, String> responseCookies;

    /**
     * Creates new instance of FrpcClient with given server configuration.
     * 
     * @param config
     */
    public FrpcClient(FrpcConfig config) {
        configuration = config;
        url = FrpcConfig.buildUrl(config);
    }

    private void prepareConnection() throws FrpcConnectionException {
        try {
            if (url != null) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);

                if (configuration.useChunkedData()) {
                    connection.setChunkedStreamingMode(0);
                }

                connection.setConnectTimeout(configuration.getConnectionTimeout());
                connection.setReadTimeout(configuration.getReadTimeout());
                connection.setRequestProperty("Content-Type", "application/x-frpc");
                connection.setRequestProperty("Accept", "application/x-frpc");
                connection.setRequestProperty("Accept-encoding", "gzip");

                if (requestCookies != null && !requestCookies.isEmpty()) {
                    String cookieString = cookiesToString(requestCookies);
                    connection.setRequestProperty("Cookie", cookieString);
                }

            } else {
                throw new FrpcConnectionException("Frpc url is null! Check your FrpcConfig.");
            }
        } catch (IOException e) {
            throw new FrpcConnectionException(e.toString());
        }
    }

    /**
     * Basic call of frpc method.
     * 
     * Returns Object as result. It's up to user to check, what it actually is.
     * 
     * 
     * @param method name of method on server
     * @param params array of method params
     * 
     * @return result as frpc object
     * 
     * @throws FrpcConnectionException
     * @throws FrpcDataException
     */
    public Object call(String method, Object... params) throws FrpcConnectionException,
            FrpcDataException {

        Object result = null;
        OutputStream out;
        InputStream in = null;
        int attempts = 0;
        boolean done = false;

        do {
            attempts++;
            try {
                prepareConnection();

                if (configuration.getBufferSize() > 0) {
                    out = new BufferedOutputStream(this.connection.getOutputStream(),
                            configuration.getBufferSize());
                } else {
                    out = new BufferedOutputStream(this.connection.getOutputStream());
                }

                FrpcBinMarshaller marshaller = new FrpcBinMarshaller(out);

                marshaller.packMagic();
                marshaller.packMethodCall(method);

                if (configuration.getParametrPrefix() != null) {
                    for (Object param : configuration.getParametrPrefix()) {
                        marshaller.packItem(param);
                    }
                }

                for (Object param : params) {
                    marshaller.packItem(param);
                }
                out.flush();

                String contentEncoding = connection.getHeaderField("Content-encoding");

                if (contentEncoding == null) {
                    if (configuration.getBufferSize() > 0) {
                        in = new BufferedInputStream(connection.getInputStream(),
                                configuration.getBufferSize());
                    } else {
                        in = new BufferedInputStream(connection.getInputStream());
                    }
                } else if (contentEncoding.equals("gzip")) {
                    if (configuration.getBufferSize() > 0) {
                        in = new BufferedInputStream(new GZIPInputStream(
                                connection.getInputStream()), configuration.getBufferSize());
                    } else {
                        in = new BufferedInputStream(new GZIPInputStream(
                                connection.getInputStream()));
                    }
                } else {
                    FrpcLog.w(LOGTAG, "Bad connnection encoding!!! - " + contentEncoding);
                }

                FrpcBinUnmarshaller unm = new FrpcBinUnmarshaller(in);
                result = unm.unmarshallObject();

                responseCookies = getCookiesFromConnection(connection);

                if (!configuration.isKeepAlive()) {
                    connection.disconnect();
                }

                done = true;

            } catch (IOException | FrpcConnectionException e) {
                if (attempts == configuration.getAttemptCount()) {

                    String msg = String.format(
                            "FrpcConnectionException after all attempts (%d): %s", attempts,
                            e.toString());
                    FrpcLog.w(LOGTAG, msg);

                    throw new FrpcConnectionException(msg);
                }
            } catch (FrpcDataException e) {
                if (attempts == configuration.getAttemptCount()) {
                    String msg = String.format("FrpcDataException after all attempts (%d): %s",
                            attempts, e.toString());
                    FrpcLog.w(LOGTAG, msg);

                    throw new FrpcDataException(msg);
                }
            }
        } while (!done && attempts <= configuration.getAttemptCount());

        return result;
    }

    /** Call frpc method with result as FrpcStruct.
     * 
     * This is useful, when you expecting result as a FrpcStruct.
     * 
     * @param method
     * @param params
     * 
     * @return result as FrpcStruct
     * 
     * @throws FrpcConnectionException
     * @throws FrpcDataException
     */
    public FrpcStruct callAsFrpcStruct(String method, Object... params)
            throws FrpcConnectionException, FrpcDataException {
        FrpcStruct resultStruct = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) call(method, params);

            if (result != null) {
                resultStruct = FrpcStruct.fromMap(result);
            } else {
                resultStruct = null;
            }
        } catch (ClassCastException e) {
            FrpcLog.e(LOGTAG, "Error in result " + e);
        }

        return resultStruct;
    }

    /** Call frpc as FrpcResult.
     * 
     * This method uses callAsFrpcStruct and catches its exceptions.
     * Exceptions are converted to status of FrpcResult.
     * 
     * 
     * @param method
     * @param params
     * 
     * @return result as FrpcResult
     */
    public FrpcResult callAsFrpcResult(String method, Object... params) {
        FrpcStruct data = null;
        FrpcResult.FrpcResultStatus status = FrpcResult.FrpcResultStatus.RESULT_OK;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) call(method, params);

            if (result != null) {
                data = FrpcStruct.fromMap(result);
            } else {
                data = null;
            }

        } catch (ClassCastException | FrpcDataException e) {
            status = FrpcResult.FrpcResultStatus.RESULT_DATA_ERROR;
        } catch (FrpcConnectionException e) {
            status = FrpcResult.FrpcResultStatus.RESULT_NETWORK_ERROR;
        }

        return new FrpcResult(data, status);
    }

    public void setRequestCookies(Map<String, String> cookies) {
        requestCookies = cookies;
    }

    public void addRequestCookie(String name, String value) {
        if (requestCookies == null) {
            requestCookies = new HashMap<String, String>();
        }

        requestCookies.put(name, value);
    }

    public Map<String, String> getResponseCookies() {
        return responseCookies;
    }

    public void clearRequestCookies() {
        requestCookies = null;
    }

    public void clearResponseCookies() {
        responseCookies = null;
    }

    private static String cookiesToString(Map<String, String> cookies) {
        return cookies.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(";", "", ";"));
    }

    private static Map<String, String> getCookiesFromConnection(URLConnection connection) {

        Map<String, List<String>> headers = connection.getHeaderFields();
        Map<String, String> cookies = null;

        List<String> cookieStrings = headers.get("Set-Cookie");

        if (cookieStrings != null && !cookieStrings.isEmpty()) {
            cookies = new HashMap<>();

            for (String cookieString : cookieStrings) {
                String[] cookieParts = cookieString.split(";");

                if (cookieParts.length > 0) {
                    String[] nameValue = cookieParts[0].split("=");
                    if (nameValue.length > 1){
                        cookies.put(nameValue[0], nameValue[1]);
                    }
                }
            }
        }

        return cookies;
    }

}
