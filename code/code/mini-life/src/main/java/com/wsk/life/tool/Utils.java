package com.wsk.life.tool;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class Utils {
	private static final String PHONE_NUMBER_REG = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$";

	private String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
			ip = request.getRemoteAddr();
			if (ip.equals("127.0.0.1")) {
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				} catch (Exception e) {
					e.printStackTrace();
				}
				ip = inet.getHostAddress();
			}
		}

		if ((ip != null) && (ip.length() > 15) && (ip.indexOf(",") > 0)) {
			ip = ip.substring(0, ip.indexOf(","));
		}

		return ip;
	}
    /**
	 * 获取本地服务P地址
	 * */
	public static String getLocalHost(){
		// 获取本机 IP 地址
		try {
			InetAddress address = InetAddress.getLocalHost();
			return address.getHostAddress();
		}
		catch (Exception ex){

		}
		return null;
	}

	public static String getOrdNo() {
		Random random = new Random();
		int x = random.nextInt(8999);
		int y = x + 1000;
		return DateUtil.getTimeStamp() + String.valueOf(y);
	}

	public static String genID() {
		Random random = new Random();
		int x = random.nextInt(899999);
		int y = x + 100000;
		return DateUtil.getDateTime() + String.valueOf(y);
	}

	public static double divide(Object obj, int divisor) {
		if ((StringUtils.isEmpty(obj)) || ("0".equals(obj))) {
			return 0.0D;
		}
		BigDecimal bdAmt = new BigDecimal(String.valueOf(obj));
		BigDecimal bdAmt2 = new BigDecimal(String.valueOf(divisor));
		BigDecimal surplus = bdAmt.divide(bdAmt2);
		return surplus.doubleValue();
	}

	public static double multiply(Object obj, int divisor) {
		if ((StringUtils.isEmpty(obj)) || ("0".equals(obj))) {
			return 0.0D;
		}
		BigDecimal bdAmt = new BigDecimal(String.valueOf(obj));
		BigDecimal bdAmt2 = new BigDecimal(String.valueOf(divisor));
		BigDecimal surplus = bdAmt.multiply(bdAmt2);
		return surplus.doubleValue();
	}

	public static String uuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static boolean isMobile(String mobile) {
		return valid("^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$", mobile);
	}

	public static boolean valid(String reg, String tmp) {
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(tmp);
		return m.matches();
	}

	public static String getRequestjournal() {
		return Long.toString(Math.abs(new Random().nextLong()));
	}

	/**
	 * 获取用户真实IP地址，不使用request.getRemoteAddr()的原因是有可能用户使用了代理软件方式避免真实IP地址,
	 * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值
	 *
	 * @return ip
	 */
	public static String getClientIp() {
		String ip = "127.0.0.1";
		try {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getRequest();
			ip = getClientIp(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ip;
	}

	/**
	 * 获取用户真实IP地址，不使用request.getRemoteAddr()的原因是有可能用户使用了代理软件方式避免真实IP地址,
	 * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值
	 *
	 * @return ip
	 */
	public static String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			if (ip.indexOf(",") != -1) {
				ip = ip.split(",")[0];
			}
		}
		String ipHeader = "";
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
			ipHeader = "Proxy-Client-IP ip:";
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
			ipHeader = "WL-Proxy-Client-IP:";
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
			ipHeader = "HTTP_CLIENT_IP:";
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			ipHeader = "HTTP_X_FORWARDED_FOR:";
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
			ipHeader = "X-Real-IP:";
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
			ipHeader = "local:";
		}
		System.out.println("获取客户端" + ipHeader + ip);
		return ip;
	}

	public static String getClientIp1(HttpServletRequest request) {
		if (request == null)
			return null;
		String ip = request.getHeader("x-forwarded-for");
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip)))
			ip = request.getHeader("Proxy-Client-IP");
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip)))
			ip = request.getHeader("WL-Proxy-Client-IP");
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip)))
			ip = request.getHeader("HTTP_CLIENT_IP");
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip)))
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip)))
			ip = request.getRemoteAddr();
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0].trim();
		}
		if (("127.0.0.1".equals(ip)) || ("0:0:0:0:0:0:0:1".equals(ip))) {
			try {
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return ip;
	}

	public static boolean isWxBrowser(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent").toLowerCase();
		return userAgent.contains("micromessenger");
	}

	public static String getAppUrl(HttpServletRequest request) {
		String url = request.getScheme() + "://" + request.getServerName();
		if ((request.getServerPort() != 80) && (request.getServerPort() != 443)) {
			url = url + ":" + request.getServerPort();
		}
		url = url + request.getContextPath();
		return url;
	}

	public static String getRandomStringByLength(int length) {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public static String YD = "^((134)|(135)|(136)|(137)|(138)|(139)|(147)|(148)|(150)|(151)|(152)|(157)|(158)|(159)|(172)|(178)|(182)|(183)|(184)|(187)|(188)|(198))\\d{8}$";
	public static String LT = "^((130)|(131)|(132)|(145)|(146)|(155)|(156)|(185)|(186)|(176)|(175)|(171)|(166))\\d{8}$";
	public static String DX = "^((133)|(149)|(153)|(173)|(177)|(180)|(181)|(189)|(199))\\d{8}$";

	public static String V_YD = "^((1703)|(1705)|(1706))\\d{7}$";
	public static String V_LT = "^((1707)|(1708)|(1709))\\d{7}$";
	public static String V_DX = "^((1700)|(1701)|(1702))\\d{7}$";

	public static String getPhoneOperator(String phone) {
		if (StringUtils.isEmpty(phone)) {
			return null;
		}

		if (phone.length() == 11) {
			if ((phone.matches(YD)) || (phone.matches(V_YD))) {
				return "0";
			}

			if ((phone.matches(LT)) || (phone.matches(V_LT))) {
				return "2";
			}

			if ((phone.matches(DX)) || (phone.matches(V_DX))) {
				return "1";
			}

			return null;
		}

		return null;
	}

	public static String[] strToArray(String separator, String str) {
		if ((StringUtils.isEmpty(separator)) || (StringUtils.isEmpty(str))) {
			return null;
		}
		return str.split(separator);
	}

	/*
	 * 获取浏览器信息
	 * */
	public static String getBrowserInfo(HttpServletRequest request) {
		try {
			String agent = request.getHeader("User-Agent").toLowerCase();
			String browser = String.format(
					"浏览器版本:%s;请求方法:%s;客户端地址:%s;脚本文件的文件路径:%s;服务器名称：%s;服务器端口号:%s;客户端IP地址:%s;客户端电脑名:%s",
					getBrowserName(agent), request.getMethod(), request.getRequestURI(), request.getServletPath(),
					request.getServerName(), request.getServerPort(), request.getRemoteAddr(), request.getRemoteHost());
			return browser;
		} catch (Exception ex) {
			return "获取浏览器信息异常:" + ex.getMessage();
		}
	}

	/*
	* 获取浏览器版本
	* */
	public static String getBrowserName(String agent) {
		if (agent.indexOf("msie 7") > 0) {
			return "ie7";
		} else if (agent.indexOf("msie 8") > 0) {
			return "ie8";
		} else if (agent.indexOf("msie 9") > 0) {
			return "ie9";
		} else if (agent.indexOf("msie 10") > 0) {
			return "ie10";
		} else if (agent.indexOf("msie") > 0) {
			return "ie";
		} else if (agent.indexOf("opera") > 0) {
			return "opera";
		} else if (agent.indexOf("opera") > 0) {
			return "opera";
		} else if (agent.indexOf("firefox") > 0) {
			return "firefox";
		} else if (agent.indexOf("webkit") > 0) {
			return "webkit";
		} else if (agent.indexOf("gecko") > 0 && agent.indexOf("rv:11") > 0) {
			return "ie11";
		} else {
			return "Others";
		}
	}

	/*
	* 替换字符串内容中间为星号
	* @param num 为前后预览位数
	* */
	public static String replaceStarChar(String content, int num) {
		int replaceNum = 2;
		if(content.length() > num*2) {
			replaceNum = content.length() - 2*num;
		}else {
			num = 1;
		}

		StringBuffer sb = new StringBuffer();
		while (replaceNum-- > 0) {
			sb.append("*");
		}
		String r = "";
		if(content.length() < 4) {
			r = content.substring(0, num) + sb.toString();
		}else {
			r = content.substring(0, num) + sb.toString() + content.substring(content.length() - num);
		}
		return r;
	}


	/**
	 * 是否存在中文
	 */
	public static boolean isExistChinese(String countname) {
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(countname);
		if (m.find()) {
			return true;
		}
		return false;
	}

	/**
	 * trim指定字符
	 */
	public static String trim(String s, char trimChar) {
		int start = 0, end = s.length() - 1;
		while (start <= end && s.charAt(start) == trimChar) {
			start++;
		}
		while (start <= end && s.charAt(end) == trimChar) {
			end--;
		}
		return s.substring(start, end + 1);
	}
	/**
	 * 将中文字符转换成Unicode字符
	 * @Title: unicodeEncode
	 * @Description: unicode编码
	 * @param string
	 * @return
	 */
	public static String unicodeEncode(String string) {
		char[] utfBytes = string.toCharArray();
		String unicodeBytes = "";
		for (int i = 0; i < utfBytes.length; i++) {
			String hexB = Integer.toHexString(utfBytes[i]);
			if (hexB.length() <= 2) {
				hexB = "00" + hexB;
			}
			unicodeBytes = unicodeBytes + "\\u" + hexB;
		}
		return unicodeBytes;
	}

	/**
	 * 将Unicode的编码转换为中文
	 * @param string
	 * @return 转换之后的内容
	 * @Title: unicodeDecode
	 * @Description: unicode解码
	 */
	public static String unicodeDecode(String string) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(string);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			string = string.replace(matcher.group(1), ch + "");
		}
		return string;
	}

	public static String convertStreamToString(InputStream inputStream,String encode){
		try {
			return IOUtils.toString(inputStream, encode);
		}
		catch (Exception ex){
			return  "";
		}
	}

	/**
	 * 功能：将输入流转换成 byte[]
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static byte[] streamToByteArray(InputStream is) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();//创建输出流对象
		byte[] b = new byte[4096];
		int len;
		while ((len = is.read(b)) != -1) {
			bos.write(b, 0, len);
		}
		byte[] array = bos.toByteArray();
		bos.close();
		return array;
	}
}
