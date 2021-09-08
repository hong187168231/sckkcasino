package com.qianyi.casinoadmin.config;

import com.qianyi.casinoadmin.util.LoginUtil;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Long authId= LoginUtil.getLoginUserId();
       if(authId==null){
           return Optional.empty();
       }

        return Optional.of(authId+"");
    }
}
