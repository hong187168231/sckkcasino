package com.qianyi.casinoadmin.inteceptor;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.modulecommon.inteceptor.AbstractAuthorizationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthorizationInteceptor extends AbstractAuthorizationInteceptor {

    @Autowired
    SysUserService sysUserService;

    @Override
    protected boolean hasBan() {
        Long authId= LoginUtil.getLoginUserId();
        SysUser user=sysUserService.findById(authId);
        boolean flag= SysUser.checkUser(user);
        return !flag;
    }

    /**
     * 此处进行权限认证
     *
     * @param request
     * @return
     */
    @Override
    public boolean hasPermission(HttpServletRequest request) {
        String token = LoginUtil.getToken();
        if(JjwtUtil.check(token, "casino-admin")){
            //进行权限认证操作


            return true;
        }

        return false;
    }


}
