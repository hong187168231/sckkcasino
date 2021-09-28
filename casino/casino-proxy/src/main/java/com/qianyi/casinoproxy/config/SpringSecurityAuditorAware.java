package com.qianyi.casinoproxy.config;

import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication==null||authentication.getPrincipal().toString().equals("anonymousUser")) {
            return Optional.empty();
        }
        log.info("authentication princial {}", authentication.getPrincipal().toString());
        Long authId = CasinoProxyUtil.getAuthId();
        return Optional.of(authId + "");
    }
}
