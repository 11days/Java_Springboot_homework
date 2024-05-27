package com.wsk.life.tool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Document工具类
 * */
public class DocumentUtil {
    /**
     * 获取元素属性值
     * */
    public static String getElementValue(Element element, String attributeName,String dataType){
        if(element == null){
            return null;
        }
        if(StringUtils.isEmptyOrWhitespace(attributeName)){
            return Utils.trim(dataType == null || dataType.contains("html") ? element.html() : element.text(),' ');
        }
        //或关系，取到值时返回
        String[] arrOr = attributeName.split("\\|");
        List<String> listAnd = new ArrayList<>();
        for(String ors : arrOr) {
            //与关系，取到值时追加
            String[] arrAnd = ors.split("\\&");
            for(String and : arrAnd){
                String value = element.attr(and);
                if(value != null){
                    listAnd.add(value);
                }
            }
            if(listAnd.size() >0){
                return String.join(",",listAnd); //默认以英文逗号分隔，如果不需要，需要使用replace_dictionary替换掉
            }
        }
        return null;
    }
    /**
     * 根据paths去获取元素取值
     * @param document
     * @param cssQuery  使用|分隔，或关系
     * @param attributeName  使用|分隔，或关系
     * @param index 为null时取所有匹配到元素时
     * */
    public static String getElementStringBySelect(Document document, String cssQuery,String attributeName, Integer index,String dataType){
        Object object = getElementObjectBySelect(document,cssQuery,attributeName,index,dataType);
        if(object != null){
            if(object.getClass().getName().contains("ArrayList")){
                return String.join("", JsonUtil.objectToListString(object));
            }
            else{
                return object.toString();
            }
        }
        return null;
    }
    /**
     * 根据paths去获取元素取值
     * @param html
     * @param cssQuery  使用|分隔，或关系
     * @param attributeName  使用|分隔，或关系
     * @param index 为null时取所有匹配到元素时
     * */
    public static Object getElementObjectBySelect(String html, String cssQuery,String attributeName, Integer index,String dataType){
        Document htmlDoc = null;
        try{
            htmlDoc = Jsoup.parse(html);
            if(htmlDoc != null){
                Object object = getElementObjectBySelect(htmlDoc,cssQuery,attributeName,index,dataType);
                return object;
            }
        }
        catch (Exception ex){
        }
        return null;
    }
    /**
     * 根据paths去获取元素取值
     * @param document
     * @param cssQuery  使用|分隔，或关系
     * @param attributeName  使用|分隔，或关系
     * @param index 为null时取所有匹配到元素时
     * */
    public static Object getElementObjectBySelect(Document document, String cssQuery,String attributeName, Integer index,String dataType){
       if(document == null || cssQuery == null){
            return null;
       }
       //或关系，取到值时返回
       String[] arrOr = cssQuery.split("\\|");
       List<String> listAnd ;
        int andValueCount = 0;
       for(String ors : arrOr){
           listAnd = new ArrayList<>();
           //与关系，取到值时追加
           String[] arrAnd = ors.split("\\&");
           for(String and : arrAnd){
               //拼接的字符串
               if(and.startsWith("\"") && and.endsWith("\"")){
                   andValueCount++;
                   listAnd.add(and.replace("\"",""));
                   continue;
               }
               Elements elements = document.select(and);
               if(elements == null || elements.size() == 0){
                   continue;
               }

               //为null，获取所有
               if(index == null){
                   for(Element item : elements){
                       String val1 = getElementValue(item,attributeName,dataType);
                       if(val1 != null){
                           andValueCount++;
                           listAnd.add(val1);
                       }
                   }
               }
               else {
                   Element element = index >= 0  && index < elements.size() ? elements.get(index) : null;
                   String val2 = getElementValue(element,attributeName,dataType);
                   if(val2 != null){
                       andValueCount++;
                       listAnd.add(val2);
                   }
               }
           }
           //如果值数量不等于And数组 数量，，则清空
           if(andValueCount != arrAnd.length){
               listAnd =new ArrayList<>();
           }
           //有值返回
           if(listAnd.size() > 0){
               return String.join("",listAnd);
           }
       }
       return null;
    }
    /**
     * 根据paths去获取元素取值
     * */
    public static Object getElementValueById(Document document, String ids,String attributeName,String dataType){
        if(document == null || ids == null){
            return null;
        }
        //或关系，取到值时返回
        String[] arrOr = ids.split("\\|");
        StringBuilder text = new StringBuilder();
        for(String ors : arrOr){
            //与关系，取到值时追加
            String[] arrAnd = ors.split("\\&");
            for(String and : arrAnd){
                Element element = document.getElementById(and);
                if(element == null){
                    continue;
                }
                String val1 = getElementValue(element,attributeName,dataType);
                if(val1 != null){
                    text.append(val1).append(";");
                }
            }
            //有值返回
            if(text.length() > 0){
                return text.toString();
            }
        }
        return null;
    }
}
