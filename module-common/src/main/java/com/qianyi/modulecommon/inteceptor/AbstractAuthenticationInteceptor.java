package com.qianyi.modulecommon.inteceptor;

import com.qianyi.modulecommon.annotation.NoAuthentication;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public abstract class AbstractAuthenticationInteceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //不是映射到方法不用拦截
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        NoAuthentication annotation = method.getAnnotation(NoAuthentication.class);
        if (annotation == null) {
            if (hasPermission(request)) {

                //帐号封号拦截
                if(hasBan()){
                    response.sendRedirect(request.getContextPath()+"/authenticationBan");
                    return false;
                }
                return true;
            }
            response.sendRedirect(request.getContextPath()+"/authenticationNopass");
            return false;
        }

        return true;


    }

    protected abstract boolean hasBan();

    public abstract boolean hasPermission(HttpServletRequest request);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
