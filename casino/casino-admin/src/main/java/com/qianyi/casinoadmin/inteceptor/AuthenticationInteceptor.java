package com.qianyi.casinoadmin.inteceptor;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {


    @Autowired
    SysUserService sysUserService;

    @Override
    protected boolean hasBan() {
        Long authId=LoginUtil.getLoginUserId();
        SysUser user=sysUserService.findById(authId);
        boolean flag= SysUser.checkUser(user);
        return !flag;
    }

    @Override
    public boolean hasPermission(HttpServletRequest request) {
        String token = LoginUtil.getToken();

        if(JjwtUtil.check(token)){
            return true;
        }
        return false;
    }
}
