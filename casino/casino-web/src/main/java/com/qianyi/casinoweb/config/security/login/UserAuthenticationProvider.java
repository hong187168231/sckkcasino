package com.qianyi.casinoweb.config.security.login;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.SpringSecurityUserDetailsService;
import com.qianyi.casinoweb.job.LoginLogJob;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private SpringSecurityUserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String userName = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        User userInfo = (User) userDetailsService.loadUserByUsername(userName);
        boolean isValid = CasinoWebUtil.checkBcrypt(password, userInfo.getPassword());
        if(!isValid){
            throw new BadCredentialsException("帐号或密码错误");
        }
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        new Thread(new LoginLogJob(ip, userInfo.getAccount(), userInfo.getId(), "casino-web")).start();

        JSONObject json=new JSONObject();
        json.put("userId",userInfo.getId());
        json.put("password",userInfo.getPassword());
        String userInfoStr = json.toJSONString();
        String token = JjwtUtil.generic(userInfoStr);
        userInfo.setToken(token);
        return new UsernamePasswordAuthenticationToken(userInfo, password, userInfo.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
