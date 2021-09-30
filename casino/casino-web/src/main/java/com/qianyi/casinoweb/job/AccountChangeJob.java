package com.qianyi.casinoweb.job;

import com.qianyi.casinocore.business.AccountChangeBusiness;
import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.service.LoginLogService;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.vo.LoginLogVo;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.util.IpUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 登陆信息处理线程
 */
@Component
public class AccountChangeJob implements AsyncService<AccountChangeVo> {

    @Autowired
    AccountChangeBusiness accountChangeBusiness;

    @Override
    public void executeAsync(AccountChangeVo vo) {
        accountChangeBusiness.save(vo);
    }
}
