package com.qianyi.modulecommon.inteceptor;

import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.UUID;

@Slf4j
public abstract class AbstractAuthenticationInteceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //日志打印traceId，同一次请求的traceId相同，方便定位日志
        ThreadContext.put("traceId", UUID.randomUUID().toString().replaceAll("-",""));
        String ip = IpUtil.getIp(request);
        String path = request.getRequestURI().replace("//", "/");
        String requestMethod = request.getMethod();
        String queryString = request.getQueryString();
        //获取请求body
//        byte[] bodyBytes = StreamUtils.copyToByteArray(request.getInputStream());
//        String body = new String(bodyBytes, request.getCharacterEncoding());
        log.info("请求IP:{},请求方法:{},请求类型:{},请求参数:{}", ip, path, requestMethod, queryString);
        //不是映射到方法不用拦截
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        NoAuthentication annotation = method.getAnnotation(NoAuthentication.class);
        if (annotation == null) {
            if (hasPermission(request,response)) {
                //帐号封号拦截
                if(hasBan()){
                    String url=request.getServletPath();
                    if(url.contains("authenticationBan")){
                        return true;
                    }
                    response.sendRedirect(request.getContextPath()+"/authenticationBan");
                    return false;
                }
                //多设备登录拦截
                if(multiDeviceCheck()){
                    return true;
                }
                response.sendRedirect(request.getContextPath()+"/authenticationMultiDevice");
                return false;
            }
            response.sendRedirect(request.getContextPath()+"/authenticationNopass");
            return false;
        }

        return true;


    }

    protected abstract boolean hasBan();

    public abstract boolean hasPermission(HttpServletRequest request, HttpServletResponse response);

    protected abstract boolean multiDeviceCheck();

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
