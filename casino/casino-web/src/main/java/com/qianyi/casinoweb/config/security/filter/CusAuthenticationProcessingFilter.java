package com.qianyi.casinoweb.config.security.filter;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinoweb.config.security.login.CusAuthenticationFailureHandler;
import com.qianyi.casinoweb.config.security.login.CusAuthenticationManager;
import com.qianyi.casinoweb.config.security.login.CusAuthenticationSuccessHandler;
import com.qianyi.casinoweb.config.security.util.MultiReadHttpServletRequest;
import com.qianyi.casinoweb.config.security.vo.PostUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class CusAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    protected CusAuthenticationProcessingFilter(CusAuthenticationManager authenticationManager, CusAuthenticationSuccessHandler cusAuthenticationSuccessHandler, CusAuthenticationFailureHandler cusAuthenticationFailureHandler) {
        super(new AntPathRequestMatcher("/login","POST"));
        this.setAuthenticationManager(authenticationManager);
        this.setAuthenticationSuccessHandler(cusAuthenticationSuccessHandler);
        this.setAuthenticationFailureHandler(cusAuthenticationFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        if (request.getContentType() == null || !request.getContentType().contains("application/json")) {
            throw new AuthenticationServiceException("请求头类型不支持: " + request.getContentType());
        }

        UsernamePasswordAuthenticationToken authRequest;
        try {
            MultiReadHttpServletRequest wrappedRequest = new MultiReadHttpServletRequest(request);
            // 将前端传递的数据转换成jsonBean数据格式
            PostUser user = JSONObject.parseObject(wrappedRequest.getBodyJsonStrByJson(wrappedRequest), PostUser.class);
            authRequest = new UsernamePasswordAuthenticationToken(user.getAccount(), user.getPassword(), null);
            authRequest.setDetails(authenticationDetailsSource.buildDetails(wrappedRequest));
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage());
        }
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
