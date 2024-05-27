package com.wsk.life.tool;

import com.wsk.life.pojo.UserInformation;
import com.wsk.life.redis.IRedisUtils;
import com.wsk.life.redis.RedisKey;
import com.wsk.life.tool.http.CookieUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.Console;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtUtil {
    @Autowired
    private  IRedisUtils _redisUtils;

    private static IRedisUtils redisUtils;
    @PostConstruct
    public  void  init(){
        redisUtils = _redisUtils;
    }



    //签名秘钥
    private static String signKey = "chongwulingyang";
    //过期时间(1天),一般30分钟
    private static Long expire = 60 * 60 * 24 * 1000L;

    /**
     * 生成JWT令牌
     * @param claims JWT第二部分负载 payload 中存储的内容
     * @return
     */
    public static String generateJwt(Map<String, Object> claims){
        String jwt = Jwts.builder()
                //自定义信息（有效载荷）
                .addClaims(claims)
                //签名算法（头部）
                .signWith(SignatureAlgorithm.HS256, signKey)
                //过期时间
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
        return jwt;
    }

    /**
     * 解析JWT令牌
     * @param jwt JWT令牌
     * @return JWT第二部分负载 payload 中存储的内容
     */
    public static Claims parseJWT(String jwt){
        Claims claims = Jwts.parser()
                //指定签名秘钥
                .setSigningKey(signKey)
                //指定JWT令牌
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }
    /**
     * 获取用户ID
     * @return
     */
    public static String getUserId(){
        //从请求里获取token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        Claims claims = parseJWT(token);
        String id = String.valueOf(claims.get("userId"));
        return id;
    }
    /**
     * 获取用户ID
     * @return
     */
    public static Long getUserIdForLong(){
        String userId = getUserId();
        return  Long.parseLong(userId);
    }
    /**
     * 获取用户ID
     * @param token
     * @return
     */
    public static String getUserId(String token){
        Claims claims = parseJWT(token);
        String id = String.valueOf(claims.get("userId"));
        return id;
    }


    /**
     * 获取用户信息
     * */
    public static UserInformation getLoginUser(HttpServletRequest request){
        UserInformation userInformation = JwtUtil.getLoginUser();
        return  userInformation;
    }
    /**
     * 获取用户信息
     * */
    public static UserInformation getLoginUser(){
        try {
            //从请求里获取token
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = CookieUtil.getCookieValue(request,"token");
            String redisKey = RedisKey.getLoginTokenKey(token);
            String userInfo = redisUtils.get(redisKey);
            //System.out.println("token=" + token + ",userInfo=" + (userInfo == null ? "无":"有"));
            return StringUtils.isEmpty(userInfo) ? null : JsonUtil.toBean(userInfo, UserInformation.class);
        }
        catch (Exception ex){
            return  null;
        }
    }
}