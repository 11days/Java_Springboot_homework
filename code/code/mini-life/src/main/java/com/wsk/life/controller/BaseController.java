package com.wsk.life.controller;

import com.wsk.life.error.LoginErrorException;
import com.wsk.life.pojo.UserInformation;
import com.wsk.life.redis.IRedisUtils;
import com.wsk.life.redis.RedisKey;
import com.wsk.life.tool.JsonUtil;
import com.wsk.life.tool.JwtUtil;
import com.wsk.life.tool.Tool;
import com.wsk.life.tool.http.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基础控制类
 */
@Controller
public class BaseController {
    @Autowired
    private  IRedisUtils _redisUtils;

    public IRedisUtils redisUtils;

    @PostConstruct
    public  void  initRedisUtils(){
        redisUtils = _redisUtils;
    }

    public HttpServletRequest init() {
        HttpServletRequest request;
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        if (null == requestAttributes) {
            throw new LoginErrorException("登录过期，请重新登录！");
        }
//        if (Tool.getInstance().isNullOrEmpty(request)) {
        request = ((ServletRequestAttributes) requestAttributes).getRequest();
//        }
        if (Tool.getInstance().isNullOrEmpty(request)) {
            throw new LoginErrorException("登录过期，请重新登录！");
        }
        if (Tool.getInstance().isNullOrEmpty(request.getSession())) {
            throw new LoginErrorException("登录过期，请重新登录！");
        }
        String nowSessionId = request.getSession().getId();
//        System.out.println(String.format("THE nowSessionId is %s, %s", new Date(), nowSessionId));
        if (StringUtils.isEmpty(nowSessionId)) {
            throw new LoginErrorException("登录过期，请重新登录！");
        }
        return request;
    }

    /**
     * 将当前的对象存储到session中
     *
     * @param name session对应的名称
     * @param t    对象
     * @param <T>  泛型
     */
    protected <T> void setToSession(String name, T t) {
        HttpServletRequest request = init();
        request.getSession().setAttribute(name, t);
    }

    /**
     * 根据当前的sessionId从redis获取该对象
     *
     * @return
     */
    protected UserInformation currentUserInfoFromRedis() {
        return JwtUtil.getLoginUser();
    }


    protected void setUserInfoToRedis(String token, UserInformation userInformation) {
        String result = JsonUtil.toJson(userInformation);
        String redisKey = RedisKey.getLoginTokenKey(token);
        redisUtils.set(redisKey, result);
    }

    protected void cleanSessionAndRedis(HttpServletResponse response) {
        HttpServletRequest request = init();
        String token = CookieUtil.getCookieValue(request,"token");
        CookieUtil.clearCookie(response,"token");
        String redisKey = RedisKey.getLoginTokenKey(token);
        redisUtils.del(redisKey);
    }

    protected String getNowSessionId() {
        HttpServletRequest request = init();
        return request.getSession().getId();
    }

    protected HttpServletRequest getRequest() {
        return init();
    }


}
