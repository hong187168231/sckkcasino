package com.qianyi.casinoweb.inteceptor;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Autowired
    UserService userService;
    @Autowired
    RedisUtil redisUtil;

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

        if(JjwtUtil.check(token, Constants.CASINO_WEB)){
            return true;
        }
        return false;
    }

    /**
     * 多设备登录校验，后面登录的会踢掉前面登录的
     * @return
     */
    @Override
    protected boolean multiDeviceCheck() {
        Long authId = CasinoWebUtil.getAuthId();
        String token = CasinoWebUtil.getToken();
        String key = Constants.REDIS_TOKEN + authId;
        Object redisToken = redisUtil.get(key);
        if (ObjectUtils.isEmpty(redisToken)) {
            return true;
        }
        if (token.equals(redisToken)) {
            return true;
        }
        return false;
    }
}
