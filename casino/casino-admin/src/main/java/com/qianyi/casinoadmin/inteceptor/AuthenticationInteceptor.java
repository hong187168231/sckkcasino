package com.qianyi.casinoadmin.inteceptor;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Override
    public boolean hasPermission(HttpServletRequest request) {
        String token = LoginUtil.getToken();

        if(JjwtUtil.check(token)){
            return true;
        }
        return false;
    }
}
