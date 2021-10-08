package com.qianyi.casinoweb.inteceptor;

import com.qianyi.casinocore.model.IpBlack;
import com.qianyi.casinocore.service.IpBlackService;
import com.qianyi.casinoweb.config.RedisLimitExcutor;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.inteceptor.AbstractIpLimitInteceptor;
import com.qianyi.modulecommon.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class IpLimitInteceptor extends AbstractIpLimitInteceptor {

    @Autowired
    private RedisLimitExcutor redisLimitExcutor;
    @Autowired
    private IpBlackService ipBlackService;

    @Override
    protected boolean ipLimit(HttpServletRequest request) {
        String ip = IpUtil.getIp(request);
        boolean access = redisLimitExcutor.tryAccess(ip);
        if(!access){
            IpBlack ipBlack =new IpBlack();
            ipBlack.setIp(ip);
            ipBlack.setStatus(Constants.no);
            ipBlack.setRemark("请求频率超过上限,封IP");
            ipBlackService.save(ipBlack);
        }
        return access;
    }
}
