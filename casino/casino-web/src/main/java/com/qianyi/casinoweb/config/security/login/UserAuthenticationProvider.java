package com.qianyi.casinoweb.config.security.login;

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

        SpringSecurityUserDetailsService.SecurityUser userInfo = (SpringSecurityUserDetailsService.SecurityUser) userDetailsService.loadUserByUsername(userName);
        boolean isValid = CasinoWebUtil.checkBcrypt(password, userInfo.getUser().getPassword());
        if(!isValid){
            throw new BadCredentialsException("密码错误！");
        }
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        new Thread(new LoginLogJob(ip, userInfo.getUser().getAccount(), userInfo.getUser().getId(), "casino-web")).start();

        String token = JjwtUtil.generic(userInfo.getUser().getId() + "");
        userInfo.setToken(token);
        return new UsernamePasswordAuthenticationToken(userInfo, password, userInfo.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
