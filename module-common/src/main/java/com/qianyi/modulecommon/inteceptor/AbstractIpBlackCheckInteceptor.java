package com.qianyi.modulecommon.inteceptor;

import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

public abstract class AbstractIpBlackCheckInteceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url=request.getServletPath();
        if(url.contains("authenticationIpLimit")){
            return true;
        }
        String ipRemark = ipBlackCheck(request);
        if(!ObjectUtils.isEmpty(ipRemark)){
//            ipRemark = URLEncoder.encode(ipRemark,"UTF-8");
            response.sendRedirect(request.getContextPath()+"/authenticationIpLimit");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    protected abstract String ipBlackCheck(HttpServletRequest request);

}
