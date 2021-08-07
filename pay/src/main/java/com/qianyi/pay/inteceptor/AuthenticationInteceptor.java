package com.qianyi.pay.inteceptor;

import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Override
    public boolean hasPermission(HttpServletRequest request) {
        String token = request.getHeader("token");

        if(JjwtUtil.check(token)){
            return true;
        }
        return false;
    }
}
