package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.ProxyHomePageReport;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyHomePageReportService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserRunningWaterService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProxyHomePageReportBusiness {

    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private UserRunningWaterService userRunningWaterService;
    @Transactional
    public ResponseEntity transferUser(Long id,Long acceptId,ProxyUser accept){
        //加锁
        ProxyUser proxyUserById = proxyUserService.findProxyUserById(id);
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
//        if (byId.getSecondProxy() != accept.getSecondProxy()){
//            this.transferAdd(homePageReports,accept.getSecondProxy(),CommonConst.NUMBER_2,accept);
//            this.transferSub(homePageReports,byId.getSecondProxy());
//        }
        //转移总代
//        if (byId.getFirstProxy() != accept.getFirstProxy()){
//            this.transferAdd(homePageReports,accept.getFirstProxy(),CommonConst.NUMBER_1,accept);
//            this.transferSub(homePageReports,byId.getFirstProxy());
//        }
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

    private  Boolean check(Long id,ProxyUser accept){
        User user = new User();
        user.setThirdProxy(id);
        List<User> userList = userService.findUserList(user, null, null);
        if (userList == null || userList.size() == CommonConst.NUMBER_0){
            return true;
        }
        userList.forEach(user1 -> {
            user1.setThirdProxy(accept.getId());
//            user1.setSecondProxy(accept.getSecondProxy());
//            user1.setFirstProxy(accept.getFirstProxy());
            userService.save(user1);
//            userRunningWaterService.updatetThirdProxy(user1.getId(),accept.getId());
        });
        userList.clear();
        return false;
    }

    private  List<ProxyUser> checkFirstProxy(ProxyUser proxyUser,List<ProxyUser> proxyUserList,ProxyUser accept){
        List<ProxyUser> proxyUsers = proxyUserService.findProxyUserList(proxyUser);
        proxyUsers = proxyUsers.stream().filter(proxy -> proxy.getProxyRole() != CommonConst.NUMBER_1).collect(Collectors.toList());
        if(proxyUsers == null || proxyUsers.size() == CommonConst.NUMBER_0){
            return null;
        }
        proxyUsers.forEach(proxyUser1 -> {
            proxyUser1.setFirstProxy(accept.getId());
            proxyUserService.save(proxyUser1);
        });
        proxyUserList.addAll(proxyUsers);
        return proxyUserList;
    }

    private synchronized List<ProxyUser> checkSecondProxy(ProxyUser proxyUser,List<ProxyUser> proxyUserList,ProxyUser accept){
        List<ProxyUser> proxyUsers = proxyUserService.findProxyUserList(proxyUser);
        if(proxyUsers == null || proxyUsers.size() == CommonConst.NUMBER_0){
            return null;
        }
        proxyUsers.forEach(proxyUser1 -> {
            proxyUser1.setSecondProxy(accept.getId());
            proxyUserService.save(proxyUser1);
        });
        proxyUserList.addAll(proxyUsers);
        return proxyUserList;
    }


    private void transferProxyAdd(List<ProxyHomePageReport> homePageReports, Long acceptId, Integer proxyRole, ProxyUser accept){
        homePageReports.forEach(proxyHomePageReport1 -> {
            ProxyHomePageReport proxyHome = proxyHomePageReportService.findByProxyUserIdAndStaticsTimes(acceptId, proxyHomePageReport1.getStaticsTimes());
            if (proxyHome == null){
                proxyHome = new ProxyHomePageReport(acceptId,proxyHomePageReport1.getStaticsTimes(),proxyHomePageReport1.getStaticsMonth(),proxyHomePageReport1.getStaticsYear(),accept.getFirstProxy(),proxyRole);
                if (proxyRole != CommonConst.NUMBER_1){
                    proxyHome.setSecondProxy(accept.getId());
                }
            }
            proxyHome.setNewUsers(proxyHome.getNewUsers() + proxyHomePageReport1.getNewUsers());
            proxyHome.setNewSecondProxys(proxyHome.getNewSecondProxys() + proxyHomePageReport1.getNewSecondProxys());
            proxyHome.setNewThirdProxys(proxyHome.getNewThirdProxys() + proxyHomePageReport1.getNewThirdProxys());
            proxyHomePageReportService.save(proxyHome);
        });
    }

    private void transferProxySub(List<ProxyHomePageReport> homePageReports,Long proxyId){
        homePageReports.forEach(proxyHomePageReport1 -> {
            ProxyHomePageReport proxyHome = proxyHomePageReportService.findByProxyUserIdAndStaticsTimes(proxyId, proxyHomePageReport1.getStaticsTimes());
            if (proxyHome != null){
                proxyHome.setNewUsers(proxyHome.getNewUsers() - proxyHomePageReport1.getNewUsers());
                proxyHome.setNewThirdProxys(proxyHome.getNewThirdProxys() - proxyHomePageReport1.getNewThirdProxys());
                proxyHomePageReportService.save(proxyHome);
            }
        });
    }

    @Transactional
    public ResponseEntity transferProxy(ProxyUser byId,ProxyUser accept){
        //加锁
        ProxyUser proxyUserById = proxyUserService.findProxyUserById(byId.getId());
        ProxyUser proxyUser = new ProxyUser();
        List<ProxyUser> proxyUserList = new ArrayList<>();
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            //总代互转
            return this.transferFirstProxy(byId,accept,proxyUser,proxyUserList);
        }else if (byId.getProxyRole() == CommonConst.NUMBER_2){
            //区域互转
            return this.transferSecondProxy(byId,accept,proxyUser,proxyUserList);
        }else {
            return ResponseUtil.custom("参数不合法");
        }
    }
    private ResponseEntity transferFirstProxy(ProxyUser byId,ProxyUser accept,ProxyUser proxyUser,List<ProxyUser> proxyUserList){
        proxyUser.setFirstProxy(byId.getId());
        //转移代理
        proxyUserList = checkFirstProxy(proxyUser,proxyUserList,accept);
        if (proxyUserList.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success();
        }
        //查询会员并转移
        User user = new User();
        user.setFirstProxy(byId.getId());
        List<User> userList = userService.findUserList(user, null, null);
        if (userList != null || userList.size() >= CommonConst.NUMBER_1){
            userList.forEach(user1 -> {
                user1.setFirstProxy(accept.getId());
                userService.save(user1);
//                userRunningWaterService.updateFirstProxy(user1.getId(),accept.getId());
            });
            userList.clear();
        }
        //查询报表并转移、转移下级、转移自己
        proxyUserList.forEach(proxyUser1 -> {
            proxyHomePageReportService.updateFirstProxy(proxyUser1.getId(),accept.getId());
        });
        List<ProxyHomePageReport> homePageReports = proxyHomePageReportService.findByProxyUserId(byId.getId());
        homePageReports = homePageReports.stream().filter(homePageReport ->(homePageReport.getNewUsers() != CommonConst.NUMBER_0
                || homePageReport.getNewSecondProxys() != CommonConst.NUMBER_0 || homePageReport.getNewThirdProxys() != CommonConst.NUMBER_0)).collect(Collectors.toList());
        if (homePageReports.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success();
        }
        this.transferProxyAdd(homePageReports,accept.getId(),accept.getProxyRole(),accept);
        homePageReports.forEach(proxyHomePageReport1 -> {
            proxyHomePageReport1.setNewUsers(CommonConst.NUMBER_0);
            proxyHomePageReport1.setNewSecondProxys(CommonConst.NUMBER_0);
            proxyHomePageReport1.setNewThirdProxys(CommonConst.NUMBER_0);
            proxyHomePageReportService.save(proxyHomePageReport1);
        });
        proxyUserService.makeZero(byId.getId());
        proxyUserService.addProxyUsersNum(accept.getId(),proxyUserList.size());
        homePageReports.clear();
        return ResponseUtil.success();
    }

    private ResponseEntity transferSecondProxy(ProxyUser byId,ProxyUser accept,ProxyUser proxyUser,List<ProxyUser> proxyUserList){
        if (byId.getFirstProxy() != accept.getFirstProxy()){
            return ResponseUtil.custom("不能跨总代理转移");
        }
        proxyUser.setSecondProxy(byId.getId());
        proxyUser.setProxyRole(CommonConst.NUMBER_3);
        //转移代理
        proxyUserList = checkSecondProxy(proxyUser,proxyUserList,accept);
        if (proxyUserList.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success();
        }
        //查询会员并转移
        User user = new User();
        user.setSecondProxy(byId.getId());
        List<User> userList = userService.findUserList(user, null, null);
        if (userList != null || userList.size() >= CommonConst.NUMBER_1){
            userList.forEach(user1 -> {
                user1.setSecondProxy(accept.getId());
                userService.save(user1);
//                userRunningWaterService.updateSecondProxy(user1.getId(),accept.getId());
            });
            userList.clear();
        }
        //查询报表并转移、转移下级、转移自己
        proxyUserList.forEach(proxyUser1 -> {
            proxyHomePageReportService.updateSecondProxy(proxyUser1.getId(),accept.getId());
        });
        List<ProxyHomePageReport> homePageReports = proxyHomePageReportService.findByProxyUserId(byId.getId());
        homePageReports = homePageReports.stream().filter(homePageReport ->(homePageReport.getNewUsers() != CommonConst.NUMBER_0
                ||  homePageReport.getNewThirdProxys() != CommonConst.NUMBER_0)).collect(Collectors.toList());
        if (homePageReports.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success();
        }
        this.transferProxyAdd(homePageReports,accept.getId(),accept.getProxyRole(),accept);
        homePageReports.forEach(proxyHomePageReport1 -> {
            proxyHomePageReport1.setNewUsers(CommonConst.NUMBER_0);
            proxyHomePageReport1.setNewThirdProxys(CommonConst.NUMBER_0);
            proxyHomePageReportService.save(proxyHomePageReport1);
        });
//            this.transferProxyAdd(homePageReports,accept.getFirstProxy(),CommonConst.NUMBER_1,accept);
//            this.transferProxySub(homePageReports,byId.getFirstProxy());

        proxyUserService.makeZero(byId.getId());
        proxyUserService.addProxyUsersNum(accept.getId(),proxyUserList.size());
//            proxyUserService.addProxyUsersNum(accept.getFirstProxy(),proxyUserList.size());
//            proxyUserService.subProxyUsersNum(byId.getFirstProxy(),proxyUserList.size());
        homePageReports.clear();
        return ResponseUtil.success();
    }
}
