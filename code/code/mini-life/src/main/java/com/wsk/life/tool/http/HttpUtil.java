package com.wsk.life.tool.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.GZIPInputStream;

import cn.hutool.core.io.file.PathUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bestvike.linq.Linq;
import com.google.common.base.Joiner;
import com.wsk.life.tool.JsonUtil;
import com.wsk.life.tool.Utils;
import lombok.var;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.util.StringUtils;
import com.alibaba.fastjson.JSON;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * @author: sadboy
 **/

public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    private static final String UTF8 = "UTF-8";
    private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String APPLICATION_JSON = "application/json; charset=UTF-8";
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";
    private static final String TEXT_HTML = "text/html; charset=UTF-8";
    private static final String APPLICATION_XML = "application/xml; charset=UTF-8";
    private static final String COOKIE_KEY = "Cookie";
    private HttpUtil() {
    }

    /**
     * 向url发送get请求,当无参数时，paramMap为NULL
     * @param url 请求url
     * @param paramMap 需要拼接的参数
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String get(String url, Map<String, String> paramMap) throws IOException {
        return get(url, paramMap, UTF8);
    }

    /**
     * 向url发送get请求
     * @param url 请求url
     * @param paramMap 需要拼接的参数
     * @param encoding 编码
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String get(String url, Map<String, String> paramMap, String encoding) throws IOException {
        encoding = encoding == null ? UTF8 : encoding;
        url = appendUrl(url, paramMap);
        return get(url, encoding, new DataParse<String>() {
            @Override
            public String parseData(HttpEntity httpEntity, String encoding) throws IOException {
                return EntityUtils.toString(httpEntity, encoding);
            }
        });
    }

    /**
     * 向url发送get请求
     * @param url 请求url
     * @param paramMap 需要拼接的参数
     * @param encoding 编码
     * @return 请求返回的字节数组，一般用于文件下载
     * @throws IOException 读写异常
     */
    public static byte[] getBinary(String url, Map<String, String> paramMap, String encoding) throws IOException {
        encoding = encoding == null ? UTF8 : encoding;
        url = appendUrl(url, paramMap);
        return get(url, encoding, new DataParse<byte[]>() {
            @Override
            public byte[] parseData(HttpEntity httpEntity, String encoding) throws IOException {
                return EntityUtils.toByteArray(httpEntity);
            }
        });
    }


    /**
     * HTTP GET 内部公共请求处理逻辑
     * @param url 请求地址
     * @param encoding 编码字符集， 默认为 utf-8
     * @param dataParse 返回数据反序列化逻辑实现类
     * @return HTTP 返回的内容
     * @throws IOException 客户端和服务器读写通讯异常
     */
    private static <T> T get(String url, String encoding, DataParse<T> dataParse) throws IOException {
        log.debug("http 请求 url: {}", url);
        T result = null;
        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建get方式请求对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Content-type", APPLICATION_JSON);
        // 通过请求对象获取响应对象
        CloseableHttpResponse response = sendRequestAndGetResult(url, httpClient, httpGet);
        // 获取结果实体
        if (null != response) {
            result = dataParse.parseData(response.getEntity(), encoding);
            if (!(result instanceof byte[])) {
                log.debug("http 请求结果: {}", result);
            } else {
                Header[] headers = response.getHeaders("Content-Type");
                for (Header responseHead : headers) {
                    String headStr = responseHead.getValue();
                    if (headStr.startsWith("application/json")) {
                        String json = new String((byte[]) result);
                        response.close();
                        throw new RuntimeException(json);
                    }
                }
            }
        }
        try {
            if (null != response) {
                response.close();
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return result;
    }


    /**
     * 向url发送post请求
     * @param url 请求url
     * @param paramMap 需要拼接的参数
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String postFormBody(String url, Map<String, String> paramMap) throws IOException {
        return postFormBody(url, paramMap, null);
    }

    /**
     * 向url发送post请求
     * @param url 请求url
     * @param paramMap 需要拼接的参数
     * @param encoding 编码
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String postFormBody(String url, Map<String, String> paramMap, String encoding) throws IOException {
        return post(url, paramMap, encoding);
    }


    /**
     * 向url发送post请求表单提交数据
     * @param url 请求url
     * @param paramMap 表单数据
     * @param encoding 编码
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    private static String post(String url, Map<String, String> paramMap, String encoding) throws IOException {
        log.debug("http 请求 url: {} , 请求参数: {}", url, appendUrl("", paramMap).replace("?", ""));
        encoding = encoding == null ? UTF8 : encoding;
        // 创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);
        // 装填参数
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        if (paramMap != null) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                String value = entry.getValue();
                //去掉如下判断会造成String类型的value为null时
                if (value != null) {
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
        }
        String domain = new URL(url).getHost();
        // 设置参数到请求对象中
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, encoding));
        // 设置header信息
        // 指定报文头【Content-type】、【User-Agent】
        httpPost.setHeader("Content-type", APPLICATION_FORM_URLENCODED);
        httpPost.setHeader("origin",domain);
        httpPost.setHeader("referer",domain);
        return post(url, httpPost, encoding, new DataParse<String>() {
            @Override
            public String parseData(HttpEntity httpEntity, String encoding) throws IOException {
                return EntityUtils.toString(httpEntity, encoding);
            }
        });
    }

    /**
     * 向url发送post请求发送json
     * @param url 请求url
     * @param json json字符串
     * @param encoding 编码
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String postJsonBody(String url, String json, String encoding) throws IOException {
        log.debug("http 请求 url: {} , 请求参数: {}", url, json);
        encoding = encoding == null ? UTF8 : encoding;
        // 创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);
        // 设置参数到请求对象中
        StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        //  Constant.UTF8
        stringEntity.setContentEncoding(encoding);
        httpPost.setEntity(stringEntity);
        String result = post(url, httpPost, encoding, new DataParse<String>() {
            @Override
            public String parseData(HttpEntity httpEntity, String encoding) throws IOException {
                return EntityUtils.toString(httpEntity, encoding);
            }
        });
        return result;
    }

    /**
     * 向url发送post请求
     * @param url 请求url
     * @param httpPost httpClient
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    private static <T> T post(String url, HttpPost httpPost, String encoding, DataParse<T> dataParse)
            throws IOException {
        T result = null;
        CloseableHttpResponse response = null;
        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        // 执行请求操作，并拿到结果（同步阻塞）
        response = sendRequestAndGetResult(url, httpClient, httpPost);
        // 获取结果实体
        // 判断网络连接状态码是否正常(0--200都数正常)
        if (null != response) {
            result = dataParse.parseData(response.getEntity(), encoding);
            log.debug("http 请求结果: {}", result);
        }
        try {
            if (null != response) {
                response.close();
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * 设置http头，发送http请求，打印请求耗时
     * @param url 请求url
     * @param httpClient httpClient
     * @param httpUriRequest httpUriRequest
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    private static CloseableHttpResponse sendRequestAndGetResult(String url, CloseableHttpClient httpClient,
            HttpUriRequest httpUriRequest) throws IOException {
        long startTime = System.currentTimeMillis();
        CloseableHttpResponse response = httpClient.execute(httpUriRequest);
        long endTime = System.currentTimeMillis();
        collectAPISpendTime(url, startTime, endTime);
        return response;
    }

    /**
     * 打印请求信息
     * @param url 请求url
     * @param startTime 请求开始时间
     * @param endTime 请求结束时间
     */
    private static void collectAPISpendTime(String url, long startTime, long endTime) {
        log.debug("HTTP请求耗时分析，请求URL: {} ， 耗时: {} ms", url, endTime - startTime);
    }

    /**
     * 向url发送post请求上传单文件
     * @param url 请求url
     * @param paramMap 需要表单提交的参数
     * @param fileMap 需要上传的文件
     * @param encoding 编码
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String postFile(String url, Map<String, String> paramMap, Map<String, File> fileMap, String encoding)
            throws IOException {
        if (fileMap != null) {
            Map<String, List<File>> fileListMap = new HashMap<String, List<File>>();
            for (Map.Entry<String, File> entry : fileMap.entrySet()) {
                File file = entry.getValue();
                List<File> fileList = new ArrayList<File>();
                fileList.add(file);
                fileListMap.put(entry.getKey(), fileList);
            }
            return postMultipleFile(url, paramMap, fileListMap, encoding);
        }
        return postMultipleFile(url, paramMap, null, encoding);
    }

    /**
     * 向url发送post请求上传多文件
     * 向url发送post请求上传单文件
     * @param url 请求url
     * @param paramMap 需要表单提交的参数
     * @param fileListMap 需要上传的文件
     * @param encoding 编码
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String postMultipleFile(String url, Map<String, String> paramMap, Map<String, List<File>> fileListMap,
            String encoding) throws IOException {
        return postFileBody(url, paramMap, fileListMap, encoding, new DataParse<String>() {
            @Override
            public String parseData(HttpEntity httpEntity, String encoding) throws IOException {
                return EntityUtils.toString(httpEntity, encoding);
            }
        });
    }

    /**
     * 向url发送post请求上传多文件
     * 向url发送post请求上传单文件
     * @param url 请求url
     * @param paramMap 需要表单提交的参数
     * @param fileListMap 需要上传的文件
     * @param encoding 编码
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    private static <T> T postFileBody(String url, Map<String, String> paramMap, Map<String, List<File>> fileListMap,
            String encoding, DataParse<T> dataParse) throws IOException {
        log.debug("http 请求 url: {} , 请求参数: {}", url, appendUrl("", paramMap).replace("?", ""));
        encoding = encoding == null ? UTF8 : encoding;
        T result = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

        ContentType contentType = ContentType.create("text/plain", Charset.forName(encoding));
        if (null != paramMap) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                entityBuilder.addTextBody(entry.getKey(), entry.getValue(), contentType);
            }
        }

        if (null != fileListMap) {
            for (Map.Entry<String, List<File>> entry : fileListMap.entrySet()) {
                String key = entry.getKey();
                List<File> fileList = entry.getValue();
                for (File file : fileList) {
                    FileBody fileBody = new FileBody(file);
                    entityBuilder.addPart(key, fileBody);
                }
            }
        }

        HttpEntity entity = entityBuilder.build();
        httpPost.setEntity(entity);
        CloseableHttpResponse response = sendRequestAndGetResult(url, httpClient, httpPost);
        if (null != response) {
            result = dataParse.parseData(response.getEntity(), encoding);
            log.debug("http 请求结果: {}", result);
        }
        try {
            if (null != response) {
                response.close();
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return result;
    }


    /**
     * 公共数据解析接口
     * @param <T>
     */
    private interface DataParse<T> {
        /**
         * 解析返回数据
         * @param httpEntity 返回实体
         * @param encoding 编码
         * @return 实际解析返回内容
         * @throws IOException io异常
         */
        T parseData(HttpEntity httpEntity, String encoding) throws IOException;

    }

    /**
     * 将url与map拼接成HTTP查询字符串
     * @param url 请求url
     * @param paramMap 需要拼装的map
     * @return 拼装好的url
     */
    public static String appendUrl(String url, Map<String, String> paramMap) throws UnsupportedEncodingException {
        if (paramMap == null) {
            return url;
        }
        StringBuffer paramStringBuffer = new StringBuffer();
        Iterator<Map.Entry<String, String>> mapIterator = paramMap.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<String, String> next = mapIterator.next();
            paramStringBuffer.append(next.getKey()).append("=").append(URLEncoder.encode(next.getValue(), "UTF-8")).append("&");
        }
        String paramStr = paramStringBuffer.toString();
//        String paramStr = mapJoinNotEncode(paramMap);
        if (paramStr != null && !"".equals(paramStr)) {
            if (url.indexOf("?") > 0) {
                if (url.endsWith("&")) {
                    url += paramStr.substring(0, paramStr.length() - 1);
                } else {
                    url += "&" + paramStr.substring(0, paramStr.length() - 1);
                }
            } else {
                url += "?" + paramStr.substring(0, paramStr.length() - 1);
            }
        }
        return url;
    }

    /**
     * 把二进制写入文件
     * @param bytes
     * @param path
     * @throws IOException
     */
    public static void writeFile(byte[] bytes, String path) throws IOException {
        OutputStream os = null;
        try {
            // 根据绝对路径初始化文件
            File localFile = new File(path);
            if (!localFile.exists()) {
                boolean newFile = localFile.createNewFile();
                if (!newFile) {
                    throw new RuntimeException("创建文件异常，路径：" + path);
                }
            }
            // 输出流
            os = new FileOutputStream(localFile);
            os.write(bytes);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }
    /**
     * 向url请求获取数据 ，支持POST
     * @param requestUrl 请求url
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String httpRequestPost(String requestUrl) throws IOException{
        return httpRequestPost(requestUrl,null,null,null);
    }
    /**
     * 向url请求获取数据 ，支持POST(form/json/xml)或GET
     * @param requestUrl 请求url
     * @param mapParams 表单数据 非必传
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String httpRequestPost(String requestUrl,Map<String, String> mapParams) throws IOException{
        return httpRequestPost(requestUrl,null,mapParams,null);
    }
    /**
     * 向url请求获取数据 ，支持POST(form/json/xml)或GET
     * @param requestUrl 请求url
     * @param mapParams 表单数据 非必传
     * @param encoding 编码 非必传,默认为UTF-8
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String httpRequestPost(String requestUrl,String encoding,Map<String, String> mapParams) throws IOException{
        return httpRequestPost(requestUrl,encoding,mapParams,null);
    }
    /**
     * 向url请求获取数据 ，支持POST(form/json/xml)或GET
     * @param requestUrl 请求url
     * @param mapParams 表单数据 非必传
     * @param mapHeaders 请求头 非必传
     * @param encoding 编码 非必传,默认为UTF-8
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String httpRequestPost(String requestUrl,String encoding,Map<String, String> mapParams, Map mapHeaders) throws IOException{
        Map<String,String> mapResult = httpRequest(requestUrl,encoding,"post",mapParams,mapHeaders);
        return  mapResult != null && mapResult.containsKey("responseText") ? mapResult.get("responseText").toString() :  null;
    }
    /**
     * 向url请求获取数据 ，支持GET
     * @param requestUrl 请求url
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String httpRequestGet(String requestUrl) throws IOException{
        return httpRequestGet(requestUrl,null,null,null);
    }
    /**
     * 向url请求获取数据 ，支持GET
     * @param requestUrl 请求url
     * @param mapParams 表单数据 非必传
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String httpRequestGet(String requestUrl,Map<String, String> mapParams) throws IOException{
        return httpRequestGet(requestUrl,null ,mapParams,null);
    }
    /**
     * 向url请求获取数据 ，支持GET
     * @param requestUrl 请求url
     * @param mapParams 表单数据 非必传
     * @param encoding 编码 非必传,默认为UTF-8
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String httpRequestGet(String requestUrl,String encoding,Map<String, String> mapParams) throws IOException{
        return httpRequestGet(requestUrl,encoding,mapParams,null);
    }
    /**
     * 向url请求获取数据 ，支持GET
     * @param requestUrl 请求url
     * @param mapParams 表单数据 非必传
     * @param mapHeaders 请求头 非必传
     * @param encoding 编码 非必传,默认为UTF-8
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String httpRequestGet(String requestUrl,String encoding,Map<String, String> mapParams, Map mapHeaders) throws IOException{
        Map<String,String> mapResult = httpRequest(requestUrl,encoding,"get",mapParams,mapHeaders);
        return  mapResult != null && mapResult.containsKey("responseText") ? mapResult.get("responseText").toString() :  null;
    }


    /**
     * 获取SSLContext
     * @return
     */
    public static CloseableHttpClient getCloseableHttpClient() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        //创建httpClient
        return HttpClients.custom().setSSLContext(sslContext).
                setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

    }
    /*
    * 获取url主域host
    * */
    public static String getHost(String url){
        if(StringUtils.isEmptyOrWhitespace(url)){
            return null;
        }
        url = url.replace("//","/");
        String[] arrUrl = url.split("\\/");
        return  url.startsWith("http") ? arrUrl[1]:arrUrl[0];
    }
    /**
     * 向url请求获取数据 ，支持POST(form/json/xml)或GET
     * @param requestUrl 请求url
     * @param mapParams 表单数据 非必传
     * @param mapHeaders 请求头 非必传
     * @param method 请求方法post或get,load 非必传
     * @param encoding 编码 非必传,默认为UTF-8
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static String httpRequestString(String requestUrl,String encoding,String method, Map<String, String> mapParams, Map mapHeaders) throws Exception {
        try{
            return httpRequest(requestUrl,encoding,method,mapParams,mapHeaders).get("responseText").toString();
        }
        catch (Exception ex){
            throw ex;
        }
    }
    /**
     * 向url请求获取数据 ，支持POST(form/json/xml)或GET
     * @param requestUrl 请求url
     * @param mapParams 表单数据 非必传
     * @param mapHeaders 请求头 非必传
     * @param method 请求方法post或get,load 非必传
     * @param encoding 编码 非必传,默认为UTF-8
     * @return 请求返回的数据
     * @throws IOException 读写异常
     */
    public static Map<String,Object> httpRequest(String requestUrl,String encoding,String method, Map<String, String> mapParams, Map<String,Object> mapHeaders) throws IOException{
        if(StringUtils.isEmptyOrWhitespace(requestUrl)){
            throw new IOException("requestUrl不能为空");
        }
        Map mapResult = new HashMap();
        //请求参数Header
        mapParams = mapParams == null ? new HashMap<>(): mapParams;
        //请求头Header
        mapHeaders = mapHeaders == null ? new HashMap<>(): mapHeaders;
        requestUrl = Utils.trim(requestUrl,'?');
        //请求方法post或get
        method = method == null ? mapParams != null && mapParams.size() > 0 ? "post" : "get" : method.toLowerCase();
        //编码
        encoding = StringUtils.isEmptyOrWhitespace(encoding) ? "UTF-8" : encoding;
        //主服务地址
        String host = getHost(requestUrl);
        //Header默认参数值
        Map<String,String> mapDefaultHeader = new HashMap();
        mapDefaultHeader.put("Accept","application/json, text/plain, */*");
        mapDefaultHeader.put("Sec-Ch-Ua-Platform","Windows");
        mapDefaultHeader.put("Sec-Fetch-Site","application/json, text/plain, */*");
        mapDefaultHeader.put("Sec-Fetch-Dest","document");
        mapDefaultHeader.put("Sec-Fetch-Mode","navigate");
        mapDefaultHeader.put("Content-Type",APPLICATION_JSON);
        mapDefaultHeader.put("Accept-Language","zh-CN,zh;q=0.9,ja;q=0.8,zh-TW;q=0.7,en;q=0.6");
        mapDefaultHeader.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
        mapDefaultHeader.put("Host",host);
        //获取header的key
        List<String> headerKeys = Linq.of(mapHeaders.entrySet()).select(r->r.getKey().toLowerCase()).toList();
        //添加默认请求头
        for(String headerName : mapDefaultHeader.keySet()){
            if(headerKeys.contains(headerName.toLowerCase()) == false){
                mapHeaders.put(headerName,mapDefaultHeader.get(headerName));
            }
        }
        //爬虫延迟时间 毫秒
        Integer crawlerInterval = mapParams.containsKey("crawlerInterval") ? Integer.parseInt(mapHeaders.get("crawlerInterval").toString()) : 100;
        //是否为登录请求
        boolean isLoginRequest = mapParams.containsKey("isLoginRequest") && mapParams.get("isLoginRequest").equals("true");
        //移除自定义参数
        mapHeaders.remove("crawlerInterval");
        mapHeaders.remove("isLoginRequest");
        HttpUriRequest httpUriRequest ;
        //post方式
        if(method.equals("post")){
            HttpPost httpPost = new HttpPost(requestUrl);
            HttpEntity httpEntity = null; //请求对象实体
            String contentType = mapHeaders.get("Content-Type").toString();
            //请求参数不为空
            if(mapParams.size() >0){
                //表单提交
                if(contentType.contains("form")){
                    // 装填参数
                    List<NameValuePair> nameValuePairs = new ArrayList<>();
                    for (Object key : mapParams.keySet()) {
                        Object value = mapParams.get(key);
                        if (value != null) {   //去掉如下判断会造成String类型的value为null时
                            nameValuePairs.add(new BasicNameValuePair(key.toString(), value.toString()));
                        }
                    }
                    //form表单提交
                    httpEntity = new UrlEncodedFormEntity(nameValuePairs, encoding);
                }
                else if(contentType.contains("xml")){ //xml方式提交
                    httpEntity = new StringEntity(JSON.toJSONString(mapParams),ContentType.create("application/xml", encoding));
                }
                else { //json方式提交
                    httpEntity = new StringEntity(JSON.toJSONString(mapParams),ContentType.create("application/json", encoding));
                }
            }
            // 设置参数到请求对象中
            httpPost.setEntity(httpEntity);
            httpUriRequest = httpPost;
        }
        else{ //url链接里参数
            if(mapParams.size() >0){
                String urlParams = Joiner.on("&").useForNull("").withKeyValueSeparator("=").join(mapParams);
                requestUrl += (requestUrl.contains("?") ? "&":"?") + urlParams;
            }
            HttpGet httpGet = new HttpGet(requestUrl);
            httpUriRequest = httpGet;
        }
        //应答内容
        String responseText = "";
        if(method.equals("load")){
            responseText = getHtmlPageResponse(requestUrl,crawlerInterval);
            //应答内容不为空时
            if(StringUtils.isEmptyOrWhitespace(responseText) == false){
                mapResult.put("responseText",responseText);
                return mapResult;
            }
        }
        //添加请求头
        for(String headerName : mapHeaders.keySet()){
            if(headerName.equals(COOKIE_KEY)){
                continue;
            }
            httpUriRequest.addHeader(headerName,mapHeaders.get(headerName).toString());
        }
        //请求内容
        HttpContext httpContext = null;
        //添加cookie
        if(isLoginRequest == false && mapHeaders.containsKey(COOKIE_KEY)){
            Object cookieObj = mapHeaders.get(COOKIE_KEY);
            if(cookieObj != null && JsonUtil.isJson(cookieObj.toString())){
                httpContext = getCookieHttpContent(cookieObj.toString());
            }
            else{
                List<String> listCookie = JsonUtil.objectToListString(mapHeaders.get(COOKIE_KEY));
                for(String cookie : listCookie){
                    httpUriRequest.addHeader(COOKIE_KEY,cookie);
                }
                mapHeaders.remove(COOKIE_KEY);
            }
        }
        CloseableHttpClient httpClient ;
        CloseableHttpResponse response ;
        try{
             httpClient = getCloseableHttpClient(); // HttpClients.createDefault();
             response = httpContext == null ? httpClient.execute(httpUriRequest) : httpClient.execute(httpUriRequest,httpContext);
        }
        catch (Exception ex){
            httpClient = getCloseableHttpClient();
            response = httpClient.execute(httpUriRequest);
        }
        //是登录请求，要获取cookie
        if(isLoginRequest){
            Header[] headers = response.getHeaders("Set-Cookie");
            if(headers != null && headers.length >0){
                List<String> cookieValues = new ArrayList<>();
                for (Header header : headers) {
                    String cookie = header.getValue();
                    cookieValues.add(cookie);
                }
                mapResult.put(COOKIE_KEY,cookieValues);
            }
        }
        Header[] responseHeaders  = response.getHeaders("Content-Encoding");
        //是否为gzip
        boolean isResponseGZip = responseHeaders != null && responseHeaders.length >0 && Linq.of(responseHeaders).any(r->r.getValue() != null && r.getValue().toLowerCase().equals("gzip"));
        HttpEntity entity = response.getEntity();

        //解压数据
        if(isResponseGZip){
            InputStream inputStream = entity.getContent(); //获取输入流
            GZIPInputStream zipInputStream = new GZIPInputStream(inputStream);
            StringBuilder stringBuilder = new StringBuilder();
            byte[] ResponseBuf = new byte[1024];
            int l = 0;
            while ((l = zipInputStream.read(ResponseBuf)) != -1) {
                stringBuilder.append(new String(ResponseBuf,encoding));
            }
            responseText = stringBuilder.toString();
        }
        else{
            try {
                responseText = EntityUtils.toString(entity, encoding);
            }
            catch (Exception ex){

            }
            if(requestUrl.startsWith("https") && (StringUtils.isEmptyOrWhitespace(responseText) || responseText.startsWith("Bad Request"))){
                HttpsRequest httpsRequest = new HttpsRequest();
                responseText = httpsRequest.httpsRequest(requestUrl,method,encoding,mapHeaders);
            }
        }
        mapResult.put("responseText",responseText);
        return mapResult;
    }
    /**
     * 设置cookie
     * */
    public static HttpContext getCookieHttpContent(String cookieJson) {
        if(cookieJson == null){
            return null;
        }
        try {
            //设置cookies
            BasicCookieStore cookieStore = new BasicCookieStore();
            JSONArray array = JSONArray.parseArray(cookieJson);
            for (int i = 0; i < array.size(); i++) {
                JSONObject json = array.getJSONObject(i);
                BasicClientCookie cookie = new BasicClientCookie(json.getString("name"), json.getString("value"));
                cookie.setDomain(json.getString("domain"));
                cookie.setPath(json.getString("path"));
                Date expiry = new Date(json.getLongValue("expiry"));
                cookie.setExpiryDate(expiry);
                cookieStore.addCookie(cookie);
            }
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            return httpContext;
        }
        catch (Exception ex){
            return null;
        }
    }
    /*
     * 延迟获取页面应答
     * */
    public static String getHtmlPageResponse(String assemblyUrl, Integer crawlerInterval) {
        crawlerInterval = crawlerInterval == null ? 30000 : crawlerInterval;
        HttpCrawlerUtils httpUtils = HttpCrawlerUtils.getInstance();
        //延迟时间
        if(crawlerInterval != null && crawlerInterval >0){
            httpUtils.setTimeout(crawlerInterval);
            httpUtils.setWaitForBackgroundJavaScript(crawlerInterval);
        }
        else{
            httpUtils.setTimeout(crawlerInterval);
            httpUtils.setWaitForBackgroundJavaScript(crawlerInterval);
        }
        try {
            String htmlPageStr = httpUtils.getHtmlPageResponse(assemblyUrl);
            return htmlPageStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getHtmlPage(String url) throws  Exception{
        JSONObject json = new JSONObject();
        json.put("name","张三");
        String jsonString = json.toString();
        // httpclient
        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
            @Override
            public boolean isTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                    throws CertificateException {
                return true;
            }
        }).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
                new String[]{"TLSv1.2", "TLSv1.1", "SSLv3"}, null, new NoopHostnameVerifier());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        HttpPost httpPost = new HttpPost(url);
        // 超时时间设置成5s
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
        httpPost.setConfig(requestConfig);
        StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        // Create a custom response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                log.info("response"+response);
                int status = response.getStatusLine().getStatusCode();
                log.info("status"+status);
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    String s = EntityUtils.toString(entity);
                    return s;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };
        String execute = httpclient.execute(httpPost, responseHandler);
        log.info("--------------------------------------111111111111111111111--------------------");
        log.info("responseBody------------>"+execute);
        return execute;

    }
    /**
     * 下载并保存图片,返回相对地址
     * */
    public static String downloadPicture(String imageUrl, String saveDirectory) throws Exception {
        String[] images = imageUrl.split("/");
        String fileName = images[images.length -1];
        //获取static资源文件目录
        String moviePath = ResourceUtils.getFile("classpath:static").getAbsolutePath();
        //文件绝对目录
        String imagePath = moviePath + "/" +  Utils.trim(saveDirectory,'/');
        //文件相对路径，包括文件名
        String relativePath =  "/" +  Utils.trim(saveDirectory,'/') + "/"+ fileName;
        File fileInfo = new File(imagePath + "/" + fileName);
        if(fileInfo.exists()){
            return relativePath;
        }
        File filePathInfo = new File(imagePath);
        //创建目录
        if(fileInfo.isDirectory() == false){
            filePathInfo.mkdirs();
        }
        HttpsURLConnection httpsURLConnection = null;
        try {
            HttpsRequest httpsRequest = new HttpsRequest();
            httpsURLConnection = httpsRequest.getHttpsConnection(imageUrl);
            httpsURLConnection.connect();
            InputStream inputStream = httpsURLConnection.getInputStream();
            BufferedInputStream in = new BufferedInputStream(inputStream);
            FileOutputStream out = new FileOutputStream(fileInfo);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            // 下载完成，关闭输入输出流
            in.close();
            out.close();
            System.out.println("图片" + imageUrl + "下载成功，保存目录路径：" + relativePath);
            return relativePath;
        }
        catch (Exception ex){
            String e = ex.getMessage();
        }
        finally {
            if(httpsURLConnection != null){
                httpsURLConnection.disconnect();
            }
        }
        return imageUrl;
    }
}

