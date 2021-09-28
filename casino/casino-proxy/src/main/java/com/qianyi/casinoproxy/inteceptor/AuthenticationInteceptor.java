package com.qianyi.casinoproxy.inteceptor;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.inteceptor.AbstractAuthenticationInteceptor;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationInteceptor extends AbstractAuthenticationInteceptor {

    @Autowired
    UserService userService;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    protected boolean hasBan() {
       Long authId= CasinoProxyUtil.getAuthId();
        User user=userService.findById(authId);
        boolean flag= User.checkUser(user);
        return !flag;
    }

    @Override
    public boolean hasPermission(HttpServletRequest request) {
        String token = CasinoProxyUtil.getToken();

        if(JjwtUtil.check(token)){
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
        Long authId = CasinoProxyUtil.getAuthId();
        String token = CasinoProxyUtil.getToken();
        String key = "token:" + authId;
        Object redisToken = null;
        try {
            redisToken = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        if (ObjectUtils.isEmpty(redisToken)) {
            return true;
        }
        if (token.equals(redisToken)) {
            return true;
        }
        return false;
    }
}