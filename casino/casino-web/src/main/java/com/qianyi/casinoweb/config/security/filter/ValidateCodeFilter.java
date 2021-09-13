package com.qianyi.casinoweb.config.security.filter;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoweb.config.security.exception.ValidateCodeException;
import com.qianyi.casinoweb.config.security.exception.ValidateCodeNotRightException;
import com.qianyi.casinoweb.config.security.login.CusAuthenticationFailureHandler;
import com.qianyi.casinoweb.config.security.util.MultiReadHttpServletRequest;
import com.qianyi.casinoweb.config.security.vo.PostUser;
import com.qianyi.moduleauthenticator.WangyiDunAuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

@Slf4j
@Component
public class ValidateCodeFilter extends OncePerRequestFilter {

    @Autowired
    private CusAuthenticationFailureHandler cusAuthenticationFailureHandler;

    public ValidateCodeFilter(CusAuthenticationFailureHandler cusAuthenticationFailureHandler) {
        this.cusAuthenticationFailureHandler = cusAuthenticationFailureHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        log.info("ValidateCodeFilter uri is {} method is {}",httpServletRequest.getRequestURI(),httpServletRequest.getMethod());
        if (StringUtils.pathEquals("/login", httpServletRequest.getRequestURI())
                && StringUtils.pathEquals(httpServletRequest.getMethod().toLowerCase(Locale.ROOT), "post")) {
            try {
                validateCode(new ServletWebRequest(httpServletRequest));
            } catch (ValidateCodeException e) {
                cusAuthenticationFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, e);
                return;
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void validateCode(ServletWebRequest servletWebRequest) throws IOException {
        MultiReadHttpServletRequest wrappedRequest = new MultiReadHttpServletRequest(servletWebRequest.getRequest());
        // 将前端传递的数据转换成jsonBean数据格式
        PostUser user = JSONObject.parseObject(wrappedRequest.getBodyJsonStrByJson(wrappedRequest), PostUser.class);
        log.info("code is {}",user.getValidate());
        if (!StringUtils.hasLength(user.getValidate())) {
            throw new ValidateCodeException("验证码不能为空！");
        }

        if(!WangyiDunAuthUtil.verify(user.getValidate())){
            throw new ValidateCodeException("验证码不正确！");
        }
    }
}
