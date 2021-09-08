package com.qianyi.casinoweb.config;

import com.qianyi.casinoweb.util.CasinoWebUtil;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Long authId= CasinoWebUtil.getAuthId();
       if(authId==null){
           return Optional.empty();
       }

        return Optional.of(authId+"");
    }
}
