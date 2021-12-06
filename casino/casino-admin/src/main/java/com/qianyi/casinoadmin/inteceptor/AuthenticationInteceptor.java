package com.qianyi.casinoadmin.inteceptor;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {


    @Autowired
    SysUserService sysUserService;

    @Autowired
    RedisUtil redisUtil;

    @Override
    protected boolean hasBan() {
        Long authId=LoginUtil.getLoginUserId();
        if(authId == null){
            return true;
        }
        SysUser user=sysUserService.findById(authId);
        boolean flag= SysUser.checkUser(user);
        return !flag;
    }

    @Override
    public boolean hasPermission(HttpServletRequest request, HttpServletResponse response) {
        String token = LoginUtil.getToken();

        if(JjwtUtil.check(token, "casino-admin")){
            return true;
        }
        return false;
    }

    @Override
    protected boolean multiDeviceCheck() {
        Long authId = LoginUtil.getLoginUserId();
        String token = LoginUtil.getToken();
        String key = Constants.REDIS_TOKEN + "admin:"+ authId;
        Object redisToken = redisUtil.get(key);
        if (ObjectUtils.isEmpty(redisToken)) {
            return true;
        }
        JjwtUtil.Token redisToken1 = (JjwtUtil.Token) redisToken;
        //新旧token有一个匹配得上说明就是最新token
        if (token.equals(redisToken1.getOldToken())||token.equals(redisToken1.getNewToken())) {
            return true;
        }
        return false;
    }
}
