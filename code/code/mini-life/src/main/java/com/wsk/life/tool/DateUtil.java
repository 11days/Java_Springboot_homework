package com.wsk.life.tool;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {

    public final static String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 时间格式(yyyy-MM-dd HH:mm:ss)
     */
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE_TIME_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    public static String format(Date date) {
        return format(date, DATE_PATTERN);
    }

    public static String format(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }

    /**
     * 获取当前时间 格式：yyyyMMddHHmmss
     *
     * @return
     */
    public static String getDateTime() {
        SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
        return DATETIME_FORMAT.format(new Date());
    }
    /**
     * 获取当前时间 格式：yyMMddHHmmss
     * @return
     */
    public static String getYyMMddHHmm() {
        SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
        return DATETIME_FORMAT.format(new Date());
    }

    /**
     * 获取当前时间 格式：HHmmss
     *
     * @return
     */
    public static String getTime() {
        SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmmss");
        return TIME_FORMAT.format(new Date());
    }

    public static String format() {
        SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmmss");
        return TIME_FORMAT.format(new Date());
    }

    public static String currentTimeMillisToDateString(long time, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);// 12小时制
        Date date = new Date();
        date.setTime(time);
        return simpleDateFormat.format(date);
    }

    /**
     * 获取当前时间 格式：yyyy-MM-dd HH:mm
     *
     * @return
     */
    public static String getShowDateTime2() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return simpleDateFormat.format(new Date());
    }

    /**
     * 获取当前时间 格式：yyyy-MM-dd HH:mm:ss sss
     *
     * @return
     */
    public static String getShowDateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    /**
     * 获取当前时间 格式：yyyy-MM
     *
     * @return
     */
    public static String getShowMonthTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        return simpleDateFormat.format(new Date());
    }

    /**
     * 获取年月 格式：yyyyMM
     * @return
     */
    public static String getYearMonth() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        return simpleDateFormat.format(new Date());
    }

    /**
     * 获取当前日期 格式：yyyyMMdd
     *
     * @return
     */
    public static String getDate() {
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        return DATE_FORMAT.format(new Date());
    }

    public static String getYYYYMMDD() {
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
        return DATE_FORMAT.format(new Date());
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    public static String getTimeStamp() {
        return String.valueOf(new Date().getTime());
    }

    /**
     * 获取昨天格式为：yyyyMMdd
     *
     * @return
     */
    public static String getYesterDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
        return DATE_FORMAT.format(cal.getTime());
    }

    /**
     * yyyyMMdd转换为yyyy年MM月dd日
     *
     * @param date
     * @return
     */
    public static String convertDate(String date) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月dd日");
        try {
            return sf.format(sf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * yyyyMMddHHmmss转换为yyyy-MM-dd HH:mm:ss
     *
     * @param datetime
     * @return
     */
    public static String convertDateTime(String datetime) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sf.format(sf.parse(datetime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 把毫秒转换为指定格式时间
     * @param timeMillis
     * @return
     */
    public static String convertDateTime(String timeMillis,String format) {
        if(StringUtils.isEmpty(timeMillis)){
            return null;
        }
        if(CheckUtil.checkIsNumberStr(timeMillis) == false){
            String[] arrTime = timeMillis.split("-");
            String[] arrFormat = format.split("-");
            if(arrFormat.length == arrTime.length){
                return  timeMillis;
            }
        }
        if(timeMillis.length() == 10){
            timeMillis = timeMillis + "000";
        }
        Long times = Long.parseLong(timeMillis);
        Date d = new Date(times);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.format(d);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    /**
     * 将时间戳转换为日期时间字符串
     * */
    public static String convertToDateTimeString(String timestamp) {
        if(timestamp.length() == 10){
            timestamp = timestamp + "000";
        }
        long timestampLong = Long.parseLong(timestamp);
        // 创建一个Date对象，将时间戳作为参数传递给构造函数
        Date date = new Date(timestampLong);
        // 创建SimpleDateFormat对象，定义日期时间的格式
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_PATTERN);
        // 使用SimpleDateFormat对象的format()方法将Date对象格式化为字符串
        String dateTimeString = sdf.format(date);
        return dateTimeString;
    }

    public static Date parse(String dateStr) {
        if(StringUtils.isEmpty(dateStr)){
            return null;
        }
        try {
            String formatStr = dateStr.length() == 19 ? DATE_TIME_PATTERN : DATE_PATTERN;
            DateFormat format = new SimpleDateFormat(formatStr);
            return format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 时间转换
     * */
    public static String convertByString(String dateStr){
        if(StringUtils.isEmpty(dateStr)){
            return null;
        }
        String dateFormat = null ;
        dateStr = dateStr.replace("T"," ");
        if(dateStr.length() > 19){
            dateStr = dateStr.substring(0,19);
        }
        //08-09
        String[] arrDate = dateStr.split("\\-");
        if(arrDate.length == 2){
            dateStr = getYear() + "-" + dateStr;
        }
        //08.09
        arrDate = dateStr.split("\\.");
        if(dateStr.length() < 6 && arrDate.length == 2){
            dateStr = getYear() + "." + dateStr;
        }
        //8月9日
        if(dateStr.contains("年") == false && dateStr.contains("月") && dateStr.contains("日")){
            dateStr = getYear() + "年" + dateStr;
        }
        if(dateStr.contains("前天")){
            dateStr = dateStr.replace("前天",getSomeDay(-2));
        }
        if(dateStr.contains("昨天")){
            dateStr = dateStr.replace("昨天",getSomeDay(-1));
        }
        if(dateStr.contains("年")){
            if(dateStr.length() == 18){
                dateStr = dateStr.replace("日"," ");
            }
            dateStr = dateStr.replace("年","-").replace("月","-").replace("日","");
        }
        //yyyy.mm.dd HH:mm:ss
        if(dateStr.contains(".") && dateStr.length() < 20){
            dateStr = dateStr.replace(".","-");
        }
        dateStr = dateStr.replace("   "," ");
        dateStr = dateStr.replace("  "," ");
        try{
            if(CheckUtil.checkIsNumberStr(dateStr)){
                if(dateStr.length() == 14){
                    dateFormat = convertDateTime(dateStr);
                }
                if(dateFormat == null){
                    dateFormat = convertToDateTimeString(dateStr);
                }
            }
            if(dateFormat == null && dateStr.contains("-"))
            {
                try {
                    if(dateStr.length() == 16){
                        dateStr = dateStr + ":00";
                    }
                    Date date = parse(dateStr);
                    if(date != null){
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_PATTERN);
                        return sdf.format(date);
                    }
                    return null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dateFormat = null;
                }
            }
            else if(dateFormat == null){
                dateFormat = convertByLatestDate(dateStr);
            }
        }
        catch (Exception ex){
            return null;
        }
        return dateFormat;
    }
    /**
     * 最近时间转换为时间格式
     * */
    public static String convertByLatestDate(String latestDate){
        try{
           String regular = "[\\d]+";
           List<String> listDate = JsonUtil.getValueByRegular(latestDate,regular);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
           if(listDate.size() >0){
                Integer value = Integer.parseInt(listDate.get(0));
                if(latestDate.contains("天前")){
                    calendar.add(Calendar.DATE, -value);
                }
                else if(latestDate.contains("秒")){
                    calendar.add(Calendar.SECOND, -value);
                }
                else if(latestDate.contains("分钟")){
                    calendar.add(Calendar.MINUTE, -value);
                }
                else if(latestDate.contains("周前")){
                    calendar.add(Calendar.DATE, -(7*value));
                }
                else if(latestDate.contains("月前")){
                   calendar.add(Calendar.MONTH, -value);
                }
                else if(latestDate.contains("年前")){
                    calendar.add(Calendar.YEAR, -value);
                }
               SimpleDateFormat dateformat = new SimpleDateFormat(DATE_TIME_PATTERN);
               return  dateformat.format(calendar.getTime());
           }
           else{
               return null;
           }
        }
        catch (Exception ex){

        }
       return null;
    }
    /**
     * 把月份范围转为日期范围
     * */
    public static String getTimeRangesMonthForDate(String timeRanges) {
        if(StringUtils.isEmpty(timeRanges)){
            return null;
        }
        try {
            String[] arrTime  = timeRanges.split("~");
            arrTime[0] = arrTime[0].trim() + "-01";
            arrTime[1] = arrTime[1].trim() + "-01";
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.parse(arrTime[1]));
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE,-1);
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            return  arrTime[0] + "~" + dateformat.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转换时间戳
     *
     * @param dateStr
     * @param pattern
     * @return
     */
    public static Date parse(String dateStr, String pattern) {

        try {
            if(StringUtils.isEmpty(dateStr)){
                return  null;
            }
            if(dateStr.length() == 16){
                pattern = DATE_TIME_YYYY_MM_DD_HH_MM;
            }
            DateFormat format = new SimpleDateFormat(pattern);
            return format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取时间所对应的毫秒
     *
     * @param datetime String 时间字符串
     * @param inFmt    String 时间格式化串
     * @return long 毫秒数
     * @throws ParseException
     */
    public static long getTimeValue(String datetime, String inFmt) throws ParseException {
        if(CheckUtil.checkIsNumber(datetime)){
            return Long.parseLong(datetime);
        }
        return parse(datetime, inFmt).getTime();
    }

    /**
     * 获取当前时间 毫秒值
     * <p>
     * 年月、月份、日期、小时、分钟、秒、毫秒
     *
     * @return
     */
    public static Long getAtPresentTime() {
        Long millis = System.currentTimeMillis();
        return millis;
    }

    /**
     * 获取某年某月的第一天 @Title:getFisrtDayOfMonth @Description: @param:@param
     * year @param:@param month @param:@return @return:String @throws
     */
    public static String getFisrtDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最小天数
        int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最小天数
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String firstDayOfMonth = sdf.format(cal.getTime());
        return firstDayOfMonth;
    }

    /**
     * 获取某月的最后一天 @Title:getLastDayOfMonth @Description: @param:@param
     * year @param:@param month @param:@return @return:String @throws
     */
    public static String getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
        return lastDayOfMonth;
    }
    /**
     * 比较时间：至分钟
     *
     * @param date1
     * @param date2
     * @return
     */
    public static long dayDiff(String date1, String date2) {
       date1 = convertByString(date1);
       date2 = convertByString(date2);
       return dayDiff(date1,date2,DATE_TIME_PATTERN);
    }
    /**
     * 比较时间：至分钟
     * @param date1
     * @param date2
     * @param format
     * @return
     */
    public static long dayDiff(String date1, String date2, String format) {
        SimpleDateFormat formater = new SimpleDateFormat(format);
        long diff = 0l;
        try {
            long d1 = formater.parse(date1).getTime();
            long d2 = formater.parse(date2).getTime();
            // diff=(Math.abs(d1-d2) / (1000 * 60 * 60 * 24));
            diff = (d1 - d2) / (1000 * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diff;
    }

    /**
     * @param date1  字符串日期1
     * @param date2  字符串日期2
     * @param format 日期格式化方式 format="yyyy-MM-dd"
     * @return
     * @descript:计算两个字符串日期相差的天数
     */
    public static long dayDiffDayNum(String date1, String date2, String format) {
        SimpleDateFormat formater = new SimpleDateFormat(format);
        long diff = 0l;
        try {
            long d1 = formater.parse(date1).getTime();
            long d2 = formater.parse(date2).getTime();
            // diff=(Math.abs(d1-d2) / (1000 * 60 * 60 * 24));
            diff = (d1 - d2) / (1000 * 60 * 60 * 24);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diff;
    }

    /**
     * @param date1  字符串日期1
     * @param date2  字符串日期2
     * @param format 日期格式化方式 format="yyyy-MM-dd"
     * @return
     * @descript:计算两个字符串日期相差的小时数
     */
    public static long hourDiffDayNum(String date1, String date2, String format) {
        SimpleDateFormat formater = new SimpleDateFormat(format);
        long diff = 0l;
        try {
            long d1 = formater.parse(date1).getTime();
            long d2 = formater.parse(date2).getTime();
            // diff=(Math.abs(d1-d2) / (1000 * 60 * 60 * 24));
            diff = (d1 - d2) / (1000 * 60 * 60);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diff;
    }

    /**
     * @param date1  字符串日期1
     * @param date2  字符串日期2
     * @param format 日期格式化方式 format="yyyy-MM-dd"
     * @return
     * @descript:计算两个字符串日期相差的分钟
     */
    public static long minuteDiffDayNum(String date1, String date2, String format) {
        SimpleDateFormat formater = new SimpleDateFormat(format);
        long diff = 0l;
        try {
            long d1 = formater.parse(date1).getTime();
            long d2 = formater.parse(date2).getTime();
            // diff=(Math.abs(d1-d2) / (1000 * 60 * 60 * 24));
            diff = (d1 - d2) / (1000 * 60);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diff;
    }
    /**
     * @param maxDate  字符串日期1
     * @param minDate  字符串日期2
     * @param format 日期格式化方式 format="yyyy-MM-dd"
     * @return
     * @descript:计算两个字符串日期相差的秒
     */
    public static long secondDiffDayNum(String maxDate, String minDate, String format) {
        if(StringUtils.isEmpty(maxDate) || StringUtils.isEmpty(minDate)){
            return 0L;
        }
        SimpleDateFormat formater = new SimpleDateFormat(format);
        long diff = 0l;
        try {
            long d1 = formater.parse(maxDate).getTime();
            long d2 = formater.parse(minDate).getTime();
            // diff=(Math.abs(d1-d2) / (1000 * 60 * 60 * 24));
            diff = (d1 - d2) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diff;
    }
    /**
     * 获取某天00:00:00
     *
     * @param day
     * @return
     */
    public static String getSomeDayFirstTime(int day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);// 这里改为1
        Date time = cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(time);
    }

    /**
     * 获取某天
     * @param day
     * @return
     */
    public static String getSomeDay(int day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);
        Date time = cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd").format(time);
    }

    /**
     * 获取前一天0点
     *
     * @param
     * @return
     */
    public static String getYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);// 这里改为1
        Date time = cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(time);
    }

    /**
     * 获取某天24:00:00
     *
     * @param day
     * @return
     */
    public static String getSomeDayLastTime(int day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);// 这里改为1
        Date time = cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd 24:00:00").format(time);
    }

    /**
     * 获取新的到期时间
     *
     * @param monthId
     * @return
     */
    public static String getNewExpireTime(Integer monthId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 获取当前日期
        Date date = new Date();
        // 创建Calendar实例
        Calendar cal = Calendar.getInstance();
        cal.setTime(date); // 设置当前时间
        cal.add(Calendar.MONTH, monthId);
        // 将时间格式化成yyyy-MM-dd HH:mm:ss的格式
        return format.format(cal.getTime());
    }

    /**
     * 获取续加到期时间
     *
     * @param
     * @param monthId
     * @return
     * @throws ParseException
     */
    public static String getContinuedExpireTime(String stringExpireTime, Integer monthId) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // 创建Calendar实例
        Calendar cal = Calendar.getInstance();
        Date dateExpireTime = format.parse(stringExpireTime);
        cal.setTime(dateExpireTime); // 设置当前时间
        cal.add(Calendar.MONTH, monthId); // 在当前时间基础上加一个月
        return format.format(cal.getTime());
    }

    /**
     * 厘转元，确保price保留两位有效数字
     *
     * @return
     */
    public static String changeL2Y(BigDecimal amount) {
        return amount.divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP) + "";
    }

    /**
     * 获取当年的第一天
     *
     * @return
     */
    public static String getCurrYearFirst() {
        Calendar currCal = Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getYearFirst(currentYear));
    }

    /**
     * 获取当年的最后一天
     *
     * @return
     */
    public static String getCurrYearLast() {
        Calendar currCal = Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getYearLast(currentYear));
    }

    /**
     * 获取某年第一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getYearFirst(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        Date currYearFirst = calendar.getTime();
        return currYearFirst;
    }

    /**
     * 获取某年最后一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getYearLast(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        Date currYearLast = calendar.getTime();

        return currYearLast;
    }
    /**
     * 获取当前年
     * @return
     */
    public static String getYear() {
        Date today = new Date();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
        try {
            String format = simpleDateFormat.format(today);
            return format;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取年
     *
     * @param date
     * @return
     */
    public static String getYear(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
        try {
            Date parse = simpleDateFormat.parse(date);
            String format = simpleDateFormat.format(parse);
            return format;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String a = "111";
        String[] split = a.split("&");
        System.out.println(split.length);
        System.out.println(minuteDiffDayNum("2020-05-25 17:01:00", "2020-05-25 17:00:00", "yyyy-MM-dd HH:mm:ss"));

        String[] s = {"12", "3"};
        try {
            System.out.println(s[-1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //获取周时间范围
    public static String getWeekScope(Integer week){
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy.01.01");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(yearSdf.format(new Date())));
        }catch (ParseException e){
            e.printStackTrace();
        }
        cal.add(Calendar.DAY_OF_MONTH, week*7);
        sb.append(sdf.format(cal.getTime()));
        sb.append(" - ");
        cal.add(Calendar.DAY_OF_MONTH, 7);
        sb.append(sdf.format(cal.getTime()));
        return sb.toString();
    }
    /*
     * 指定时间加分钟 并返回新时间
     * */
    public static Date setMinutes(String time,Integer minutes){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 日期格式
        try{
            Date dateTime = dateFormat.parse(time); // 指定日期
            Calendar cl = Calendar.getInstance();
            cl.setTime(dateTime);
            cl.add(Calendar.MINUTE,minutes);
            return cl.getTime();
        }
        catch (Exception ex){
            return  null;
        }
    }
    /*
    * 指定时间加天数 并返回新时间
    * */
    public static Date setDays(String time,Integer days){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 日期格式
        try{
            Date dateTime = dateFormat.parse(time); // 指定日期
            Calendar cl = Calendar.getInstance();
            cl.setTime(dateTime);
            cl.add(Calendar.DATE,days);
            return cl.getTime();
        }
        catch (Exception ex){
            return  null;
        }
    }
    /*
     * 指定时间加天数 并返回新时间
     * */
    public static String setDaysFormat(String time,Integer days){
        Date newTime = setDays(time,days);
        return format(newTime,DATE_TIME_PATTERN);
    }
    /*
     * 指定时间加天数 并返回新时间
     * */
    public static String setDaysFormat(String time,Integer days,String format){
        Date newTime = setDays(time,days);
        return format(newTime,format);
    }

    /**将日期进行累加并返回list
     * @param dateTime
     * @param addDays
     * @return
     */
    public static List<String> dateAddTimeToList(String dateTime,int addDays){
    	List<String> list = new ArrayList<String>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(dateTime);
			Calendar calendar =Calendar.getInstance();

			for (int i = 1; i <= addDays; i++) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, i);
				list.add(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DATE));
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

    	return list;
    }

    /**
     * 将时间加减
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static String diffDateTime(int year,int month,int day,int hour,int minute,int second){
    	Calendar calendar =Calendar.getInstance();
    	calendar.add(Calendar.YEAR, year);
    	calendar.add(Calendar.MONTH, month);
    	calendar.add(Calendar.DATE, day);
    	calendar.add(Calendar.HOUR_OF_DAY, hour);
    	calendar.add(Calendar.MINUTE, minute);
    	calendar.add(Calendar.SECOND, second);
    	return format(calendar.getTime(), DATE_TIME_PATTERN);
    }

    public static String getYYYY_MM() {
    	SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM");
    	return DATE_FORMAT.format(new Date());
    }

    /**
     * 判断一个时间是否在某个时间区间内
     * @param dateTime
     * @param starTime
     * @param endTime
     * @return
     */
    public static boolean hasBetween(String dateTime,String starTime,String endTime) {
    	boolean b = false;
    	try {
    	    if(dateTime.length() == 10){
                dateTime = dateTime + " 00:00:00";
            }
            if(starTime.length() == 10){
                starTime = starTime + " 00:00:00";
            }
            if(dateTime.length() == 10){
                endTime = endTime + " 00:00:00";
            }
			Long time1 = getTimeValue(dateTime, DATE_TIME_PATTERN);
			Long time2 = getTimeValue(starTime, DATE_TIME_PATTERN);
			Long time3 = getTimeValue(endTime, DATE_TIME_PATTERN);
			if(time2 <= time1 && time3 >= time1 ) {
				b = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return b;


    }
    /**
     * 判断一个时间是否在某个时间年月区间内
     * @param dateTime
     * @param starTime
     * @param endTime
     * @return
     */
    public static boolean hasBetweenYearMonth(String dateTime,Date starTime,Date endTime) {
        String st = format(starTime,"yyyy-MM-dd");
        String et = format(endTime,"yyyy-MM-dd");
        return  hasBetweenYearMonth(dateTime,st,et);
    }
    /**
     * 判断一个时间是否在某个时间年月区间内
     * @param dateTime
     * @param starTime
     * @param endTime
     * @return
     */
    public static boolean hasBetweenYearMonth(String dateTime,String starTime,String endTime) {
        boolean b = false;
        try {
            if(StringUtils.isEmpty(dateTime)){
                return  false;
            }
            Integer dateTimeInt = Integer.parseInt(dateTime.split(" ")[0].replace("-",""));
            Integer starTimeInt =  Integer.parseInt(starTime.split(" ")[0].replace("-",""));
            Integer endTimeInt  =  Integer.parseInt(endTime.split(" ")[0].replace("-",""));
            if(dateTimeInt < starTimeInt){
                dateTimeInt = starTimeInt;
            }
            if(starTimeInt <= dateTimeInt && endTimeInt >= dateTimeInt ) {
                b = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;

    }

    /**
     * 把日期转换为字符串
     *
     * @param date
     * @return
     */
    public static String dateToString(Date date, String format) {
        if (date == null) {
            return null;
        }
        String result = "";
        SimpleDateFormat formater = new SimpleDateFormat(format);
        try {
            result = formater.format(date);
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * 获取时间差耗时(秒)
     * */
    public static Integer getSpendTimes(Date startTime,Date endTime){

        try{
            Long seconds = (endTime.getTime() - startTime.getTime()) / 1000;
            return Integer.parseInt(seconds.toString());
        }
        catch(Exception ex) {
            return 0;
        }
    }
}
