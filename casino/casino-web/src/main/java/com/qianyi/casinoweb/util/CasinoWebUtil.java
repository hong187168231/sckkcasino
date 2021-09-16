package com.qianyi.casinoweb.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.util.ExpiringMapUtil;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;

public class CasinoWebUtil {

    public final static String salt = "f44grgr";
    public final static String auth_header = "authorization";

    //获取当前操作者的身份
    public static Long getAuthId() {
        String token = getToken();
        if (checkNull(token)) {
            return null;
        }
        String subStr = JjwtUtil.parse(token);
        JjwtUtil.Subject subject = JSON.parseObject(subStr, JjwtUtil.Subject.class);
        Long userId = Long.parseLong(subject.getUserId());
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