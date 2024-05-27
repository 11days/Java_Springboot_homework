package com.wsk.life.tool.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import com.wsk.life.tool.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class HttpsRequest {

    private final Logger logger = LoggerFactory.getLogger(HttpsRequest.class);
    public HttpsURLConnection getHttpsConnection(String requestUrl ){
        return getHttpsConnection(requestUrl,"GET","UTF-8",null);
    }
    public HttpsURLConnection getHttpsConnection(String requestUrl ,String requestMethod, String encode, Map<String,Object> mapHeaders){
        HttpsURLConnection httpsConnection = null;
        try {
            // 创建SSLContext
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = { new TrustManager() };
            // 初始化
            sslContext.init(null, tm, new java.security.SecureRandom());

            // 获取SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            HostnameVerifier HostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String var1, SSLSession var2) {
                    return true;
                }
            };
            URL url = new URL(requestUrl);
            httpsConnection = (HttpsURLConnection) url.openConnection();
            httpsConnection.setDoOutput(false);
            httpsConnection.setDoInput(true);
            httpsConnection.setConnectTimeout(10000);
            httpsConnection.setReadTimeout(10000);
            if(mapHeaders != null && mapHeaders.size() >0){
                for(String key : mapHeaders.keySet()){
                    httpsConnection.setRequestProperty(key,  mapHeaders.get(key).toString());
                }
            }
            else{
                httpsConnection.setRequestProperty("Content-Type", "text/html; charset=utf-8");
                httpsConnection.setRequestProperty("Charset", encode);
                httpsConnection.setRequestProperty("Authorization", "Basic aXdidXNlcjp0ZXN0MDAwMA==");
                httpsConnection.setRequestProperty("User-Agent", "Client identifier");
                httpsConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
            }
            httpsConnection.setRequestMethod(requestMethod.toUpperCase());
            // 设置当前实例使用的SSLSoctetFactory
            httpsConnection.setSSLSocketFactory(ssf);
            httpsConnection.setHostnameVerifier(HostnameVerifier);
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("connect local ezcs service exception: " + e.getMessage());
            return null;
        }
        return httpsConnection;
    }

    /**
     * 处理https GET/POST请求
     * @param requestUrl 请求地址
     * @param requestMethod 请求方法
     * @return
     */
    public String httpsRequest(String requestUrl, String requestMethod, String encode, Map<String,Object> mapHeaders) {
        HttpsURLConnection httpsConnection = null;
        try {
            httpsConnection = getHttpsConnection(requestUrl,requestMethod,encode,mapHeaders);
            httpsConnection.connect();
            // 往服务器端写内容
            // 读取服务器端返回的内容
            InputStream inputStream = httpsConnection.getInputStream();
            if (httpsConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("connect ezcs service failed: " + httpsConnection.getResponseCode());
                return null;
            }
            String response = Utils.convertStreamToString(inputStream, encode);
            logger.debug("response from ezcs service: " + response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("connect local ezcs service exception: " + e.getMessage());
            return null;
        } finally {
            if (httpsConnection != null) {
                httpsConnection.disconnect();
            }
        }
    }

    /**
     * 处理https GET请求
     * @return
     */
    public OutputStream getOutputStream(String requestUrl) throws Exception{
        HttpsURLConnection httpsConnection = null;
        try {
            httpsConnection = getHttpsConnection(requestUrl);
            httpsConnection.connect();
            // 往服务器端写内容
            // 读取服务器端返回的内容
            OutputStream outputStream = httpsConnection.getOutputStream();
            return outputStream;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("connect local ezcs service exception: " + e.getMessage());
            return null;
        } finally {
            if (httpsConnection != null) {
                httpsConnection.disconnect();
            }
        }
    }
    /**
     * 处理https GET请求
     * @return
     */
    public InputStream getInputStream(String requestUrl) throws Exception{
        HttpsURLConnection httpsConnection = null;
        try {
            httpsConnection = getHttpsConnection(requestUrl);
            httpsConnection.connect();
            // 往服务器端写内容
            // 读取服务器端返回的内容
            InputStream inputStream = httpsConnection.getInputStream();
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("connect local ezcs service exception: " + e.getMessage());
            return null;
        } finally {
            if (httpsConnection != null) {
                httpsConnection.disconnect();
            }
        }
    }


    /**
     * 处理https GET/POST请求
     * @param requestUrl 请求地址
     * @param requestMethod 请求方法
     * @param requestStr 请求参数
     * @return
     */
    public JSONObject httpsRequestForJsonObject(String requestUrl, String requestMethod, String requestStr) {
        logger.info("req---->:" + requestMethod + requestStr);
        HttpsURLConnection httpsConnection = null;
        try {
            httpsConnection = getHttpsConnection(requestUrl);
            httpsConnection.connect();
            // 往服务器端写内容
            // 读取服务器端返回的内容
            InputStream inputStream = httpsConnection.getInputStream();
            if (httpsConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("connect ezcs service failed: " + httpsConnection.getResponseCode());
                JSONObject responseJson = new JSONObject();
//                responseJson.put(ResponseKey.KEY_RESULT,
//                        com.ricoh.rapp.unifiedPlatform.dsdkService.constant.HttpConstant.ResultCode.ERROR_SERVER_HTTP_ERROR);
                return responseJson;
            }
            String response = Utils.convertStreamToString(inputStream, "UTF-8");
            logger.debug("response from ezcs service: " + response);
            JSONObject responseJson = JSON.parseObject(response);
            return responseJson;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("connect local ezcs service exception: " + e.getMessage());
            JSONObject responseJson = new JSONObject();
//            if (e instanceof SocketTimeoutException || e instanceof SocketException) {
//                responseJson.put(ResponseKey.KEY_RESULT,
//                        com.ricoh.rapp.unifiedPlatform.dsdkService.constant.HttpConstant.ResultCode.ERROR_SERVER_HTTP_ERROR);
//            } else {
//                responseJson.put(ResponseKey.KEY_RESULT,
//                        com.ricoh.rapp.unifiedPlatform.dsdkService.constant.HttpConstant.ResultCode.ERROR_INNER_ERROR);
//            }
            return responseJson;
        } finally {
            if (httpsConnection != null) {
                httpsConnection.disconnect();
            }
        }
    }

    class TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }

    }

    public static class ResponseKey {
        public static final String KEY_RESULT = "result";
        public static final String KEY_REASON = "reason";
        public static final String KEY_DATA = "data";
        public static final String KEY_EXTRA = "extra";
    }

}