package com.qianyi.payadmin.inteceptor;

import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Override
    public boolean hasPermission(HttpServletRequest request) {
        String token = request.getHeader("authorization");

        if(JjwtUtil.check(token)){
            return true;
        }
        return false;
    }
}
