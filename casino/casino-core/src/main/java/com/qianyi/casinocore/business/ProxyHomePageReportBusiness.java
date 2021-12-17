package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.ProxyHomePageReport;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyHomePageReportService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProxyHomePageReportBusiness {

    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    @Autowired
    private UserService userService;

    @Transactional
    public ResponseEntity transferUser(Long id,Long acceptId,ProxyUser accept,ProxyUser byId){
        if (check(id,accept)){
            return ResponseUtil.success();
        }
        ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
        proxyHomePageReport.setProxyUserId(id);
        List<ProxyHomePageReport> homePageReports = proxyHomePageReportService.findHomePageReports(proxyHomePageReport, null, null);
        homePageReports = homePageReports.stream().filter(homePageReport ->homePageReport.getNewUsers() != CommonConst.NUMBER_0).collect(Collectors.toList());
        if (homePageReports.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success();
        }
        //转移基层
        this.transferAdd(homePageReports,acceptId,CommonConst.NUMBER_3,accept);
        //转移区域
        if (byId.getSecondProxy() != accept.getSecondProxy()){
            this.transferAdd(homePageReports,accept.getSecondProxy(),CommonConst.NUMBER_2,accept);
            this.transferSub(homePageReports,byId.getSecondProxy());
        }
        //转移总代
        if (byId.getFirstProxy() != accept.getFirstProxy()){
            this.transferAdd(homePageReports,accept.getFirstProxy(),CommonConst.NUMBER_1,accept);
            this.transferSub(homePageReports,byId.getFirstProxy());
        }
        homePageReports.forEach(proxyHomePageReport1 -> {
            proxyHomePageReport1.setNewUsers(CommonConst.NUMBER_0);
            proxyHomePageReportService.save(proxyHomePageReport1);
        });
        homePageReports.clear();
        return ResponseUtil.success();
    }

    private void transferAdd(List<ProxyHomePageReport> homePageReports, Long acceptId, Integer proxyRole, ProxyUser accept){
        homePageReports.forEach(proxyHomePageReport1 -> {
            ProxyHomePageReport proxyHome = proxyHomePageReportService.findByProxyUserIdAndStaticsTimes(acceptId, proxyHomePageReport1.getStaticsTimes());
            if (proxyHome == null){
                proxyHome = new ProxyHomePageReport(acceptId,proxyHomePageReport1.getStaticsTimes(),proxyHomePageReport1.getStaticsMonth(),proxyHomePageReport1.getStaticsYear(),accept.getFirstProxy(),proxyRole);
                if (proxyRole != CommonConst.NUMBER_1){
                    proxyHome.setSecondProxy(accept.getSecondProxy());
                }
            }
            proxyHome.setNewUsers(proxyHome.getNewUsers() + proxyHomePageReport1.getNewUsers());
            proxyHomePageReportService.save(proxyHome);
        });
    }

    private void transferSub(List<ProxyHomePageReport> homePageReports,Long proxyId){
        homePageReports.forEach(proxyHomePageReport1 -> {
            ProxyHomePageReport proxyHome = proxyHomePageReportService.findByProxyUserIdAndStaticsTimes(proxyId, proxyHomePageReport1.getStaticsTimes());
            if (proxyHome != null){
                proxyHome.setNewUsers(proxyHome.getNewUsers() - proxyHomePageReport1.getNewUsers());
                proxyHomePageReportService.save(proxyHome);
            }
        });
    }

    private synchronized Boolean check(Long id,ProxyUser accept){
        User user = new User();
        user.setThirdProxy(id);
        List<User> userList = userService.findUserList(user, null, null);
        if (userList == null || userList.size() == CommonConst.NUMBER_0){
            return true;
        }
        userList.forEach(user1 -> {
            user1.setThirdProxy(accept.getId());
            user1.setSecondProxy(accept.getSecondProxy());
            user1.setFirstProxy(accept.getFirstProxy());
            userService.save(user1);
        });
        userList.clear();
        return false;
    }
}
