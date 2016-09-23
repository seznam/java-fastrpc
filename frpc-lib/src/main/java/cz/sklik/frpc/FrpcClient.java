package cz.sklik.frpc;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.Map.Entry;
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
 * Frpc structure is used as HashMap<String, Object>, frpc array as Object[].
 * Datetime is used as GregorianCalendar.
 * 
 * @author Jakub Janda
 * 
 * @see FrpcAsyncTask
 */
public class FrpcClient {

    static String                   LOGTAG    = "FRPC";

    static String                   LOGRESULT = "FRPCRESULT";

    private FrpcConfig              mConfiguration;

    private HttpURLConnection       mConnection;

    private URL                     mUrl;

    private HashMap<String, String> mRequestCookies;

    private HashMap<String, String> mResponseCookies;

    /**
     * Creates new instance of FrpcClient with given server configuration.
     * 
     * @param config
     */
    public FrpcClient(FrpcConfig config) {
        mConfiguration = config;
        mUrl = FrpcConfig.buildUrl(config);
    }

    /**
     * Sets new frpc configuration.
     * 
     * @param config
     */
    public void setConfig(FrpcConfig config) {
        mConfiguration = config;
        mUrl = FrpcConfig.buildUrl(config);
    }

    private void prepareConnection() throws FrpcConnectionException {
        try {
            if (mUrl != null) {
                mConnection = (HttpURLConnection)mUrl.openConnection();
                mConnection.setDoInput(true);
                mConnection.setDoOutput(true);

                if (mConfiguration.mUseChunkedData) {
                    mConnection.setChunkedStreamingMode(0);
                }

                mConnection.setConnectTimeout(mConfiguration.mConnectionTimeout);
                mConnection.setReadTimeout(mConfiguration.mReadTimeout);
                mConnection.setRequestProperty("Content-Type", "application/x-frpc");
                mConnection.setRequestProperty("Accept", "application/x-frpc");
                mConnection.setRequestProperty("Accept-encoding", "gzip");

                if (mRequestCookies != null && !mRequestCookies.isEmpty()) {
                    String cookieString = cookiesToString(mRequestCookies);
                    mConnection.setRequestProperty("Cookie", cookieString);
                }

            } else {
                throw new FrpcConnectionException("Frpc url is null! Check your FrpcConfig.");
            }
        } catch (MalformedURLException e) {
            throw new FrpcConnectionException(e.toString());
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

                if (mConfiguration.mBufferSize > 0) {
                    out = new BufferedOutputStream(this.mConnection.getOutputStream(),
                            mConfiguration.mBufferSize);
                } else {
                    out = new BufferedOutputStream(this.mConnection.getOutputStream());
                }

                FrpcBinMarshaller marshaller = new FrpcBinMarshaller(out);

                marshaller.packMagic();
                marshaller.packMethodCall(method);

                if (mConfiguration.mParamPrefix != null) {
                    for (Object param : mConfiguration.mParamPrefix) {
                        marshaller.packItem(param);
                    }
                }

                for (Object param : params) {
                    marshaller.packItem(param);
                }
                out.flush();

                String contentEncoding = mConnection.getHeaderField("Content-encoding");

                if (contentEncoding == null) {
                    if (mConfiguration.mBufferSize > 0) {
                        in = new BufferedInputStream(mConnection.getInputStream(),
                                mConfiguration.mBufferSize);
                    } else {
                        in = new BufferedInputStream(mConnection.getInputStream());
                    }
                } else if (contentEncoding.equals("gzip")) {
                    if (mConfiguration.mBufferSize > 0) {
                        in = new BufferedInputStream(new GZIPInputStream(
                                mConnection.getInputStream()), mConfiguration.mBufferSize);
                    } else {
                        in = new BufferedInputStream(new GZIPInputStream(
                                mConnection.getInputStream()));
                    }
                } else {
                    FrpcLog.w(LOGTAG, "Bad connnection encoding!!! - " + contentEncoding);
                }

                FrpcBinUnmarshaller unm = new FrpcBinUnmarshaller(in);
                result = unm.unmarshallObject();

                mResponseCookies = getCookiesFromConnection(mConnection);

                if (!mConfiguration.mKeepAlive) {
                    mConnection.disconnect();
                }

                done = true;

            } catch (IOException e) {
                if (attempts == mConfiguration.mAttempts) {

                    String msg = String.format(
                            "FrpcConnectionException after all attempts (%d): %s", attempts,
                            e.toString());
                    FrpcLog.w(LOGTAG, msg);

                    throw new FrpcConnectionException(msg);
                }
            } catch (FrpcDataException e) {
                if (attempts == mConfiguration.mAttempts) {
                    String msg = String.format("FrpcDataException after all attempts (%d): %s",
                            attempts, e.toString());
                    FrpcLog.w(LOGTAG, msg);

                    throw new FrpcDataException(msg);
                }
            } catch (FrpcConnectionException e) {
                if (attempts == mConfiguration.mAttempts) {

                    String msg = String.format(
                            "FrpcConnectionException after all attempts (%d): %s", attempts,
                            e.toString());
                    FrpcLog.w(LOGTAG, msg);

                    throw new FrpcConnectionException(msg);
                }
            }
        } while (!done && attempts <= mConfiguration.mAttempts);

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
     * 
     * @see call
     */
    public FrpcStruct callAsFrpcStruct(String method, Object... params)
            throws FrpcConnectionException, FrpcDataException {
        FrpcStruct resultStruct = null;

        try {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> result = (HashMap<String, Object>)call(method, params);

            if (result != null) {
                resultStruct = FrpcStruct.fromHashMap(result);
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
     * 
     * @see callAsFrpcStruct
     */
    public FrpcResult callAsFrpcResult(String method, Object... params) {
        FrpcStruct data = null;
        FrpcResult.FrpcResultStatus status = FrpcResult.FrpcResultStatus.ResultOk;

        try {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> result = (HashMap<String, Object>)call(method, params);

            if (result != null) {
                data = FrpcStruct.fromHashMap(result);
            } else {
                data = null;
            }

        } catch (ClassCastException e) {
            status = FrpcResult.FrpcResultStatus.ResultDataError;
        } catch (FrpcConnectionException e) {
            status = FrpcResult.FrpcResultStatus.ResultNetworkError;
        } catch (FrpcDataException e) {
            status = FrpcResult.FrpcResultStatus.ResultDataError;
        }

        FrpcResult result = new FrpcResult(data, status);
        return result;
    }

    public void setRequestCookies(HashMap<String, String> cookies) {
        mRequestCookies = cookies;
    }

    public void addRequestCookie(String name, String value) {
        if (mRequestCookies == null) {
            mRequestCookies = new HashMap<String, String>();
        }

        mRequestCookies.put(name, value);
    }

    public HashMap<String, String> getResponseCookies() {
        return mResponseCookies;
    }

    public void clearRequestCookies() {
        mRequestCookies = null;
    }

    public void clearResponseCookies() {
        mResponseCookies = null;
    }

    private static String cookiesToString(HashMap<String, String> cookies) {

        StringBuilder sb = new StringBuilder();

        Set<Entry<String, String>> pairs = cookies.entrySet();

        for (Iterator<Entry<String, String>> i = pairs.iterator(); i.hasNext();) {
            Entry<String, String> entry = i.next();

            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }

        return sb.toString();
    }

    private static HashMap<String, String> getCookiesFromConnection(URLConnection connection) {

        Map<String, List<String>> headers = connection.getHeaderFields();
        HashMap<String, String> cookies = null;

        List<String> cookieStrings = headers.get("Set-Cookie");

        if (cookieStrings != null && !cookieStrings.isEmpty()) {
            cookies = new HashMap<String, String>();

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
