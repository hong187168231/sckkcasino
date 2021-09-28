package com.qianyi.casinoweb.job;

import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.service.LoginLogService;
import com.qianyi.casinoweb.vo.LoginLogVo;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulecommon.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 登陆信息处理线程
 */
@Component
public class LoginLogJob implements AsyncService<LoginLogVo> {

    @Autowired
    LoginLogService loginLogService;

    @Override
    public void executeAsync(LoginLogVo vo) {

        LoginLog loginLog = new LoginLog();

        loginLog.setIp(vo.getIp());

        loginLog.setAccount(vo.getAccount());
        loginLog.setUserId(vo.getUserId());
        loginLog.setDescription(vo.getRemark());

        String address = IpUtil.getAddress(vo.getIp());
        if (address != null) {
            loginLog.setAddress(address);
        }
        if(vo.getType()!=null){
            loginLog.setType(vo.getType());
        }

        loginLogService.save(loginLog);

    }

}
