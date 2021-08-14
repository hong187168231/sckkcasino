package com.qianyi.pay.util;

import com.qianyi.modulecommon.util.IpUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;

public class PayUtil {

    public final static String salt = "f44grgr";

    //获取当前操作者的身份
    public static Long getAuthId() {
        return null;
    }

    public static boolean checkNull(Object... obj) {
        if (obj.length < 1) {
            return false;
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
        return BCrypt.checkpw(value, bcryptValue);
    }

    public static String getCapatchKey(HttpServletRequest request, String code) {
        String ip = IpUtil.getIp(request);
        return ip + code;
    }
}
