package com.qianyi.casinoweb.inteceptor;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Autowired
    UserService userService;

    @Override
    protected boolean hasBan() {
       Long authId=CasinoWebUtil.getAuthId();
        User user=userService.findById(authId);
        boolean flag= User.checkUser(user);
        return !flag;
    }

    @Override
    public boolean hasPermission(HttpServletRequest request) {
        String token = CasinoWebUtil.getToken();

        if(JjwtUtil.check(token)){
            return true;
        }
        return false;
    }
}
