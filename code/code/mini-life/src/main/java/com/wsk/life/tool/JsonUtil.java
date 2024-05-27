package com.wsk.life.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @DESCRIPTION :
 * @AUTHOR : sk
 * @TIME : 2017/12/31  18:22
 */
public class JsonUtil {

    private static Gson gson = null;

    static {
        gson = new Gson();//todo yyyy-MM-dd HH:mm:ss
    }

    public static synchronized Gson newInstance() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public static <T> String toJson(T obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> T toBean(String json, Class<T> clz) {
        return JSONObject.parseObject(json, clz);
//        return gson.fromJson(json, clz);
    }

    public static <T> Map<String, T> toMap(String json, Class<T> clz) {
        Map<String, JsonObject> map = gson.fromJson(json, new TypeToken<Map<String, JsonObject>>() {
        }.getType());
        Map<String, T> result = new HashMap<>();
        for (String key : map.keySet()) {
            result.put(key, gson.fromJson(map.get(key), clz));
        }
        return result;
    }

    public static Map<String, Object> toMap(String json) {
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
        return map;
    }

    public static <T> List<T> toList(String json, Class<T> clz) {
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        List<T> list = new ArrayList<>();
        for (final JsonElement elem : array) {
            list.add(gson.fromJson(elem, clz));
        }
        return list;
    }
    /**
     * object转List方法
     */
    public static List<String> objectToListString(Object object) {
        List<String> list = new ArrayList();
        try {
            for (Object o : (List<?>) object) {
                list.add(o.toString());
            }
            return list;
        }catch (Exception ex){
            list.add(object.toString());
        }
        return null;
    }

    /**
     * 判断是否为JSON格式
     * */
    public static boolean isJson(String text){
        if(StringUtils.isEmptyOrWhitespace(text)){
            return false;
        }
        try{
            text = Utils.trim(text,' ');
            if(text.startsWith("[") && text.endsWith("]")){
                text = String.format("{\"assemblyList\":%s}",text);
            }
            if(text.startsWith("{") && text.endsWith("}")){
                //json字符串转为json对象的方法，转换失败，则不是json格式
                JSONObject parse = (JSONObject) JSONObject.parse(text);
                return parse != null ? true : false;
            }
            return false;

        }catch(Exception e){
            return false;
        }
    }

    /**
     * 通过正则表达式获取数据
     * */
    public static List<String> getValueByRegular(Object contentObject, String regular){
        List<String> listContent = new ArrayList<>();
        try {
            String contentString;
            String typeName = contentObject.getClass().getName();
            if (typeName.contains("String")) {
                contentString = contentObject.toString();
            } else if (typeName.contains("JSONObject")) {
                contentString = JSONObject.toJSONString(contentObject, SerializerFeature.WriteMapNullValue);
            } else {
                contentString = JSONObject.toJSONString(contentObject);
            }

            Pattern pattern = Pattern.compile(regular,Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contentString);
            while (matcher.find()) {
                listContent.add(matcher.group(0));
            }
            return listContent;
        }
        catch (Exception ex){
            return listContent;
        }
    }

}