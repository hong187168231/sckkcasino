package com.qianyi.modulecommon.inteceptor;

import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.util.IpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class AbstractAuthenticationInteceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        printRequestlog(request);
        //不是映射到方法不用拦截
        log.info("handler instanceof HandlerMethod : {}", handler instanceof HandlerMethod);
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String url = request.getServletPath();
        log.info("url :{}", url);
        if (url.contains("authenticationPlatformMaintain")) {
            return true;
        }
        log.info(" check come here.");
        //平台维护开关校验
        PlatformMaintenanceSwitch maintain = platformMaintainCheck();
        Boolean onOff = maintain.getOnOff();
        if (onOff) {
            response.sendRedirect(request.getContextPath() + "/authenticationPlatformMaintain?startTime="+maintain.getStartTime()+"&endTime="+maintain.getEndTime());
            return false;
        }
        log.info(" check platform switch status : {}", onOff);
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        NoAuthentication annotation = method.getAnnotation(NoAuthentication.class);
        log.info("annotation : {}", annotation);
        if (annotation == null) {
            boolean hasPermission = hasPermission(request, response);
            log.info("hasPermission(request,response) : {}", hasPermission);
            if (hasPermission) {
                //帐号封号拦截
                boolean hasBan = hasBan();
                log.info("hasBan : {}", hasBan);
                if (hasBan) {
                    if (url.contains("authenticationBan")) {
                        return true;
                    }
                    log.info("url.contains(\"authenticationBan\") : {}", url.contains("authenticationBan"));
                    response.sendRedirect(request.getContextPath() + "/authenticationBan");
                    return false;
                }
                //多设备登录拦截
                log.info("检查多设备登录拦截");
                if (multiDeviceCheck()) {
                    return true;
                }
                log.info("检查多设备登录拦截 -end");
                response.sendRedirect(request.getContextPath() + "/authenticationMultiDevice");
                return false;
            }
            log.info("if end");
            response.sendRedirect(request.getContextPath() + "/authenticationNopass");
            return false;
        }
        log.info("end.");
        return true;


    }

    protected abstract boolean hasBan();

    public abstract boolean hasPermission(HttpServletRequest request, HttpServletResponse response);

    protected abstract boolean multiDeviceCheck();

    protected PlatformMaintenanceSwitch platformMaintainCheck(){
        return new PlatformMaintenanceSwitch();
    };

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    @Data
    public static class PlatformMaintenanceSwitch {

        private Boolean onOff = Boolean.FALSE;

        private String startTime;

        private String endTime;
    }

    /**
     * 打印请求信息
     * @param request
     */
    private void printRequestlog(HttpServletRequest request) {
        try {
            //日志打印traceId，同一次请求的traceId相同，方便定位日志
            ThreadContext.put("traceId", UUID.randomUUID().toString().replaceAll("-", ""));
            String ip = IpUtil.getIp(request);
            String path = request.getRequestURI().replace("//", "/");
            String requestMethod = request.getMethod();
            String query = request.getQueryString();
            String queryString = null;
            if (!ObjectUtils.isEmpty(query)) {
                queryString = URLDecoder.decode(request.getQueryString(), "UTF-8");//将中文转码
            }
            Map<String, String> map = new HashMap<String, String>();
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = (String) headerNames.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
//            StringBuffer body = new StringBuffer();
//            String line = null;
//            BufferedReader reader = request.getReader();
//            while ((line = reader.readLine()) != null) {
//                body.append(line);
//            }
            log.info("请求IP:{},请求方法:{},请求类型:{},请求路径参数:{},请求头:{}", ip, path, requestMethod, queryString, map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
