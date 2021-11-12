package com.qianyi.casinoproxy.util;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.ExpiringMapUtil;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.MessageFormat;

@Slf4j
@Component
public class CasinoProxyUtil {

    public static CasinoProxyUtil casinoProxyUtil;
    @Autowired
    private ProxyUserService proxyUserService;

    public final static String salt = "f44grgr";
    public final static String auth_header = "authorization";
    private static final String SET = "set";
    private static final String METHOD_FORMAT = "{0}{1}";
    private static final String FIRSTPROXY = "firstProxy";
    private static final String SECONDPROXY = "secondProxy";
    private static final String THIRDPROXY = "thirdProxy";
    //获取当前操作者的身份
    public static Long getAuthId() {
        String token = getToken();
        return getAuthId(token);
    }
    @PostConstruct //初始化
    public void init() {
        casinoProxyUtil = this;
        casinoProxyUtil.proxyUserService = this.proxyUserService;
    }
    public static Boolean setParameter(Object object){
        ProxyUser proxyUser = casinoProxyUtil.proxyUserService.findById(getAuthId());
        return setParameter(object,proxyUser);
    }
    public static Boolean setParameter(Object object,ProxyUser proxyUser){
        if (CasinoProxyUtil.checkNull(proxyUser)){
            return true;
        }
        if (proxyUser.getProxyRole() == CommonConst.NUMBER_1){
            return setMethod(object,FIRSTPROXY, proxyUser.getId());
        }else if(proxyUser.getProxyRole() == CommonConst.NUMBER_2){
            return setMethod(object,SECONDPROXY, proxyUser.getId());
        }else if(proxyUser.getProxyRole() == CommonConst.NUMBER_3){
            return setMethod(object,THIRDPROXY, proxyUser.getId());
        }else {
            return true;
        }
    }
    private static Boolean setMethod(Object object,String parameter,Long proxyRole){
        try {
            Method setMethod = object.getClass().getMethod(MessageFormat.format(METHOD_FORMAT, SET,
                    CommonUtil.toUpperCaseFirstOne(parameter)), Long.class);
            setMethod.invoke(object, proxyRole);
        } catch (Exception e) {
            log.error("反射生成对象异常",e);
            return true;
        }
        return false;
    }
    //获取当前操作者的身份
    public static Long getAuthId(String token) {
        JjwtUtil.Subject subject = JjwtUtil.parse(token, "casino-proxy");
        if (subject == null) {
            return null;
        }
        String userIds = subject.getUserId();
        Long userId = null;
        if (!CasinoProxyUtil.checkNull(userIds)) {
            userId = Long.parseLong(userIds);
        }
        return userId;
    }

    public static boolean checkNull(Object... obj) {
        if (obj == null || obj.length < 1) {
            return true;
        }
        for (Object v : obj) {
            if (ObjectUtils.isEmpty(v)) {
                return true;
            }
        }
        return false;
    }

    //加密
    public static String bcrypt(String value) {
        return BCrypt.hashpw(value, BCrypt.gensalt());
    }

    //校验加密
    public static boolean checkBcrypt(String value, String bcryptValue) {
        if (checkNull(value) || checkNull(bcryptValue)) {
            return false;
        }
        return BCrypt.checkpw(value, bcryptValue);
    }

    public static String getCaptchaKey(HttpServletRequest request, String code) {
        String ip = IpUtil.getIp(request);
        return ip + code;
    }

    public static String getToken() {
        String token = getRequest().getHeader(auth_header);
        if (!checkNull(token)) {
            token = token.replaceAll("Bearer ", "");
        }
        return token;
    }

    public static String getToken(String token) {
        if (!checkNull(token)) {
            token = token.replaceAll("Bearer ", "");
        }
        return token;
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static Pageable setPageable(Integer pageCode, Integer pageSize, Sort sort) {

        if (pageSize == null || pageCode == null) {
            pageCode = 1;
            pageSize = 10;
        }

        if (pageCode < 1 || pageSize < 1) {
            pageCode = 1;
            pageSize = 10;
        }

        if (pageSize > 100) {
            pageSize = 100;
        }

        Pageable pageable = PageRequest.of(pageCode - 1, pageSize, sort);
        return pageable;
    }

    public static Pageable setPageable(Integer pageCode, Integer pageSize) {

        return setPageable(pageCode, pageSize, null);
    }

    public static boolean checkCaptcha(String captchaCode, String captchaText) {
        HttpServletRequest request = getRequest();
        String captchaKey = getCaptchaKey(request, captchaCode);
        String text = ExpiringMapUtil.getMap().get(captchaKey);
        if (checkNull(text)) {
            return false;
        }
        if (!(captchaText.equals(text))) {
            return false;
        }
        try {
            ExpiringMapUtil.getMap().remove(captchaKey);
        } catch (Exception e) {
            return true;
        }
        return true;
    }
}