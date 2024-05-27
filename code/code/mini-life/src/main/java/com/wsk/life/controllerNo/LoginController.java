package com.wsk.life.controllerNo;

import com.wsk.life.controller.UserInformationController;
import com.wsk.life.token.TokenProccessor;
import com.wsk.life.tool.http.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用戶登錄，不需要驗證信息
 */
@Controller
public class LoginController {

    private boolean hasCookie = true;

    @Autowired
    private UserInformationController userInformationController;

    @RequestMapping(value = {"/", "login"}, method = RequestMethod.GET)
    public String login(Model model, HttpServletRequest request, HttpServletResponse response) {
        String username = CookieUtil.getCookieValue(request,"MovieWeb_username");
        String password = CookieUtil.getCookieValue(request,"MovieWeb_password");
        String remember_me = CookieUtil.getCookieValue(request,"remember_me");
        model.addAttribute("username",username);
        model.addAttribute("password",password);
        model.addAttribute("remember_me",remember_me);
        model.addAttribute("error","");
        return "login";
    }

    public boolean isHasCookie() {
        return hasCookie;
    }

    public void setHasCookie(boolean hasCookie) {
        this.hasCookie = hasCookie;
    }
}
