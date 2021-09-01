package com.qianyi.casinoweb.inteceptor;

import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Override
    public boolean hasPermission(HttpServletRequest request) {
        String token = CasinoWebUtil.getToken();

        if(JjwtUtil.check(token)){
            return true;
        }
        return false;
    }
}
