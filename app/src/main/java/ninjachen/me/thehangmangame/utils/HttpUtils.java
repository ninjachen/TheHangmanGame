package ninjachen.me.thehangmangame.utils;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieStore;

import ninjachen.me.thehangmangame.MyApplication;

/**
 * Created by Ninja on 2015/3/15.
 */
public class HttpUtils {
    private static final String TAG = "HttpUtils";
    //单例的apache-httpclient
    private static HttpClient defaultHttpClient;
    private static HttpContext localContext;
//    final public static CookieStore LOCAL_COOKIE_STORE = new PersistentCookieStore(MyApplication.getInstance());

    public static HttpClient getHttpClient() {
        if (defaultHttpClient != null)
            return defaultHttpClient;
        //创建一个本地上下文信息
        localContext = new BasicHttpContext();
        //在本地上下问中绑定一个本地存储
//        localContext.setAttribute(ClientContext.COOKIE_STORE, GeexCache.LOCAL_COOKIE_STORE);
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        defaultHttpClient = new DefaultHttpClient(params);
        return defaultHttpClient;
    }

    /**
     * a REST-STYLE http get request
     * all the params will be shown like {url}/{params0}/{params1} ...
     *
     * @param url
     * @param params
     * @return if return null, error occurs
     */
    public static String callInHTTPGet(String url, String... params) {
        // Add your data
        if (params != null && params.length > 0) {
            for (String value : params) {
                url += "/";
                url += value;
            }
        }
        // Create a new HttpClient and Post Header
        HttpClient httpclient = getHttpClient();
        HttpGet httpGet = new HttpGet(url);

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httpGet, localContext);
            Log.i(TAG, "response bode : " + inputStreamToString(response.getEntity().getContent()).toString());
            if (isResponseOK(response)) {
                //request OK
                return inputStreamToString(response.getEntity().getContent()).toString();
            } else {
                Log.e(TAG, String.format("callInHTTPGet run into error, error code is %dw", response.getStatusLine().getStatusCode()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "callInHTTPGet " + e.getMessage());
        }
        //error
        return null;
    }

    /**
     * a http post request
     * all the params will be shown like {url}/{params0}/{params1} ...
     *
     * @param url
     * @param params
     * @return if return null, error occurs
     */
    public static String callInHTTPPost(String url, JSONObject params) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = getHttpClient();
        HttpPost httppost = new HttpPost(url);
//        httppost.addHeader("Authorization", "your token"); //CSRF TOKEN
        httppost.addHeader("Content-Type", "application/json");
//        httppost.addHeader("User-Agent", "imgfornote");
        try {
            if (params != null && params.length() > 0){
                Log.d(TAG, "invoke a HTTP-POST : " + params.toString());
                httppost.setEntity(new StringEntity(params.toString()));
            }
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost, localContext);
            String responStr = inputStreamToString(response.getEntity().getContent()).toString();
            Log.d(TAG, "response code: " + response.getStatusLine().getStatusCode() + "; response body : " + responStr);
            if (isResponseOK(response)) {
                //request OK
                return responStr;
            } else {
                Log.e(TAG, String.format("callInHTTPPost run into error, error code is %d", response.getStatusLine().getStatusCode()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "callInHTTPPost " + e.getMessage());
        }
        //error
        return null;
    }

    private static boolean isResponseOK(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() >= 200
                && response.getStatusLine().getStatusCode() < 300) {
            return true;
        }
        return false;
    }

    // Fast Implementation
    private static StringBuilder inputStreamToString(InputStream is) {
        try {
            String line;
            StringBuilder total = new StringBuilder();

            // Wrap a BufferedReader around the InputStream
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            // Read response until the end
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }

            // Return full string
            return total;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
