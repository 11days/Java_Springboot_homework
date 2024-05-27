package com.wsk.life.tool;


import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * by Toxicant-yanbin
 * 校验工具类
 */
public class CheckUtil {

    /**
     * 校验是否是邮箱格式
     *
     * @param email 邮箱
     * @return true-是邮箱，false-不是邮箱
     */
    public static boolean checkIsEmail(String email) {
        boolean tag = true;
        final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        final Pattern pattern = compile(pattern1);
        final Matcher mat = pattern.matcher(email);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    /**
     * 验证手机号是否正确
     *
     * @param phone 手机号
     * @return true-是手机号，false-不是手机号
     */
    public static boolean checkIsPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return false;
        }
        String regex = "^((13[0-9])|(14[0,1,4-9])|(15[0-3,5-9])|(16[2,5,6,7])|(17[0-8])|(18[0-9])|(19[0-3,5-9]))\\d{8}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(phone);
        return m.matches();
        //        return phone.length()==11 && phone.startsWith("1");
//        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
//        String regex = "^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$";
//        if (phone.length() != 11) {
//            return false;
//        } else {
//            Pattern p = compile(regex);
//            Matcher m = p.matcher(phone);
//            boolean isMatch = m.matches();
//            if (!isMatch) {
//                return false;
//            }
//        }
//        return true;
    }

    /**
     * 验证是否是数字
     *
     * @param number 数字
     * @return true-是数字，false-不是数字
     */
    public static boolean checkIsNumber(String number) {
        if (StringUtils.isEmpty(number)) {
            return false;
        }
        try {
            Double.parseDouble(number);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 验证是否是数字
     *
     * @param number 数字
     * @return true-是数字，false-不是数字
     */
    public static boolean checkIsNumberStr(String number) {
        if (StringUtils.isEmpty(number)) {
            return false;
        }
        try {
            //判断存在中文才翻译，如果是纯数字的不翻译
            String pattern = "-?[1-9]\\d*";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(number);
            return m.matches();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证是否是数字
     *
     * @param text 文本
     * @return true，false
     */
    public static boolean checkContent(String text,String regular) {
        try {
            Pattern r = Pattern.compile(regular);
            Matcher m = r.matcher(text);
            return m.matches();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验某个数是非在某些状态中
     *
     * @param param  需要被校验的数
     * @param params 校验池
     * @return true-存在，false-不存在
     */
    public static boolean checkInItems(Object param, Object... params) {
        try {
            if (ObjectUtils.isEmpty(params)) {
                return false;
            }
            if (Arrays.asList(params).contains(param)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 校验是否是身份证格式
     *
     * @param idNo 身份证号
     * @return true-是，false-不是
     */
    public static boolean checkIsIdCard(String idNo) {

        // 中国公民身份证格式：长度为15或18位，最后一位可以为字母
        Pattern idNoPattern = compile("(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])");

        // 格式验证
        if (!idNoPattern.matcher(idNo).matches()) {
            return false;
        }

        // 合法性验证

        int year = 0;
        int month = 0;
        int day = 0;

        if (idNo.length() == 15) {

            // 一代身份证

            System.out.println("一代身份证：" + idNo);

            // 提取身份证上的前6位以及出生年月日
            Pattern birthDatePattern = compile("\\d{6}(\\d{2})(\\d{2})(\\d{2}).*");

            Matcher birthDateMather = birthDatePattern.matcher(idNo);

            if (birthDateMather.find()) {

                year = Integer.parseInt("19" + birthDateMather.group(1));
                month = Integer.parseInt(birthDateMather.group(2));
                day = Integer.parseInt(birthDateMather.group(3));

            }

        } else if (idNo.length() == 18) {

            // 二代身份证

            System.out.println("二代身份证：" + idNo);

            // 提取身份证上的前6位以及出生年月日
            Pattern birthDatePattern = compile("\\d{6}(\\d{4})(\\d{2})(\\d{2}).*");

            Matcher birthDateMather = birthDatePattern.matcher(idNo);

            if (birthDateMather.find()) {

                year = Integer.parseInt(birthDateMather.group(1));
                month = Integer.parseInt(birthDateMather.group(2));
                day = Integer.parseInt(birthDateMather.group(3));
            }

        }

        // 年份判断，100年前至今

        Calendar cal = Calendar.getInstance();

        // 当前年份
        int currentYear = cal.get(Calendar.YEAR);

        if (year <= currentYear - 100 || year > currentYear) {
            return false;
        }

        // 月份判断
        if (month < 1 || month > 12) {
            return false;
        }

        // 日期判断

        // 计算月份天数

        int dayCount = 31;

        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                dayCount = 31;
                break;
            case 2:
                // 2月份判断是否为闰年
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                    dayCount = 29;
                    break;
                } else {
                    dayCount = 28;
                    break;
                }
            case 4:
            case 6:
            case 9:
            case 11:
                dayCount = 30;
                break;
            default:
                break;
        }

        System.out.println(String.format("生日：%d年%d月%d日", year, month, day));
        System.out.println(month + "月份有：" + dayCount + "天");

        if (day < 1 || day > dayCount) {
            return false;
        }

        return true;
    }


    /**
     * 检验是否是时间格式
     *
     * @param date 日期
     * @return true-是时间，false-不是时间
     */
    public static boolean checkIsTime(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-HH-dd HH:mm:ss");
        try {
            simpleDateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }


    /**
     * 检验是否是日期格式
     *
     * @param str 日期
     * @return true-是日期，false-不是日期
     */
    public static boolean checkIsDate(String str) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            formatter.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ip校验
     * @param ipAddress 单个ip
     * @return
     */
    public static boolean isValidIPAddress(String ipAddress) {
        if ((ipAddress != null) && (!ipAddress.isEmpty())) {
            return Pattern.matches("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$", ipAddress);
        }
        return false;
    }

    /**
     * IP校验
     * @param str 字符串多个ip ,分割
     * @return
     */
    public static boolean isValidIPAddressMore(String str) {
        String[] ips = str.split(",");
        for (String ipAddress : ips) {
            if (Pattern.matches("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$", ipAddress)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}

