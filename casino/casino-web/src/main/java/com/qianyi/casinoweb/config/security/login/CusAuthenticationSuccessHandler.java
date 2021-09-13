package com.qianyi.casinoweb.config.security.login;

import com.qianyi.casinocore.service.SpringSecurityUserDetailsService;
import com.qianyi.casinoweb.config.security.util.ApiResult;
import com.qianyi.casinoweb.config.security.util.ResponseUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CusAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        SpringSecurityUserDetailsService.SecurityUser securityUser = (SpringSecurityUserDetailsService.SecurityUser) authentication.getPrincipal();
        ResponseUtils.out(httpServletResponse, ApiResult.ok("登录成功！",securityUser.getToken()));
    }
}
