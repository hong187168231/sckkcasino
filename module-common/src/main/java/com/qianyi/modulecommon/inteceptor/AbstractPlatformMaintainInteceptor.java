package com.qianyi.modulecommon.inteceptor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * KK平台维护开关检查
 */
@Slf4j
public abstract class AbstractPlatformMaintainInteceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getServletPath();
        if (url.contains("authenticationPlatformMaintain")) {
            return true;
        }
        PlatformMaintenanceSwitch maintain = platformMaintainCheck();
        Boolean onOff = maintain.getOnOff();
        if (onOff) {
            log.error("KK平台维护中,维护时间{} ~ {}", maintain.getStartTime(), maintain.getEndTime());
            response.sendRedirect(request.getContextPath() + "/authenticationPlatformMaintain?onOff=" + onOff + "&startTime=" + maintain.getStartTime() + "&endTime=" + maintain.getEndTime());
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

    protected abstract PlatformMaintenanceSwitch platformMaintainCheck();


    @Data
    public static class PlatformMaintenanceSwitch {

        private Boolean onOff = Boolean.FALSE;

        private String startTime;

        private String endTime;
    }
}
