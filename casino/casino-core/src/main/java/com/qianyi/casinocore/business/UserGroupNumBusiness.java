package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserGroupNumBusiness {

    @Autowired
    private ProxyReportService proxyReportService;

    /**
     * 内部调用，单线程
     * 统计新增金额，人数
     */
    public void addUserGroupNum(User user){
        log.info("新增的用户对象：【{}】", JSONObject.toJSON(user));
        if(user == null){
            return;
        }

        //代理团队添加代理操作
        if(user.getFirstPid() == null || user.getFirstPid() == 0){
            return;
        }
        //是否首冲
        setProxyReport(user.getFirstPid(), true);
        if(user.getSecondPid() == null || user.getSecondPid() == 0){
            return;
        }
        setProxyReport(user.getFirstPid(), false);

        if(user.getThirdPid() == null || user.getThirdPid() == 0){
            return;
        }
        setProxyReport(user.getFirstPid(), false);
    }

    private void setProxyReport(Long userid, boolean flag) {
        ProxyReport proxyReport = proxyReportService.findByUserId(userid);
        if(proxyReport == null){
            proxyReport = new ProxyReport();
            proxyReport.setAllBetNum(1);
            if(flag){
                proxyReport.setDirectChargeNum(1);
            }else{
                proxyReport.setOtherChargeNum(1);
            }
        }else{
            proxyReport.setAllChargeNum(proxyReport.getAllChargeNum() + 1);
            if(flag){
                proxyReport.setDirectChargeNum(proxyReport.getDirectGroupNum() + 1);
            }else{
                proxyReport.setOtherChargeNum(proxyReport.getOtherChargeNum() + 1);
            }
        }
        log.info("首冲添加人数：【{}】", proxyReport);
        proxyReportService.save(proxyReport);
    }
}
