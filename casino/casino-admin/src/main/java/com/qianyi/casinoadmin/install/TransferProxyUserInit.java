package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyCommission;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyCommissionService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@Order(value = 10)
public class TransferProxyUserInit implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyCommissionService proxyCommissionService;

    public static final String firstProxy1Name = "ror";
    public static final String secondProxy1Name = "zhenhao01dai04";
    @Override
    public void run(String... args) throws Exception {



        ProxyUser firstProxy1 = proxyUserService.findByUserName(firstProxy1Name);
        if (LoginUtil.checkNull(firstProxy1) || firstProxy1.getProxyRole() != CommonConst.NUMBER_1){
            log.error("找不到总代{}",firstProxy1Name);
        }else {
            ProxyUser secondProxy1 = proxyUserService.findByUserName(secondProxy1Name);
            if (!LoginUtil.checkNull(secondProxy1) && secondProxy1.getProxyRole() == CommonConst.NUMBER_1){
                log.info("开始转移{}",secondProxy1Name);
                secondProxyToFirstProxy(secondProxy1,firstProxy1);
            }
        }
//        ProxyUser chun888ZD = proxyUserService.findByUserName("chun888ZD");
//        if (LoginUtil.checkNull(chun888ZD) || chun888ZD.getProxyRole() != CommonConst.NUMBER_1){
//            log.error("找不到总代chun888ZD");
//        }else {
//            ProxyUser ajiaoQD = proxyUserService.findByUserName("ajiaoQD");
//            if (!LoginUtil.checkNull(ajiaoQD) && ajiaoQD.getProxyRole() == CommonConst.NUMBER_1){
//                log.info("开始转移ajiaoQD");
//                secondProxyToFirstProxy(ajiaoQD,chun888ZD);
//            }
//        }
//
//        ProxyUser mrhope = proxyUserService.findByUserName("mrhope");
//        if (LoginUtil.checkNull(mrhope) || mrhope.getProxyRole() != CommonConst.NUMBER_1){
//            log.error("找不到总代mrhope");
//        }else {
//            ProxyUser simaQD = proxyUserService.findByUserName("simaQD");
//            if (!LoginUtil.checkNull(simaQD) && simaQD.getProxyRole() == CommonConst.NUMBER_1){
//                log.info("开始转移simaQD");
//                secondProxyToFirstProxy(simaQD,mrhope);
//            }
//        }

    }

    private void secondProxyToFirstProxy(ProxyUser secondProxy,ProxyUser firstProxy){
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setSecondProxy(secondProxy.getId());
        proxyUser.setProxyRole(CommonConst.NUMBER_3);
        List<ProxyUser> proxyUsers = proxyUserService.findProxyUserList(proxyUser);
        if(proxyUsers == null || proxyUsers.size() == CommonConst.NUMBER_0){
            log.info("转移代理被转移者id{}账号{}无下级",secondProxy.getId(),secondProxy.getUserName());
            return ;
        }
        log.info("转移代理被转移者id{} 下级数量{}",proxyUser.getSecondProxy(),proxyUsers.size());
        proxyUsers.forEach(proxyUser1 -> {
            proxyUser1.setFirstProxy(firstProxy.getId());
            proxyUserService.save(proxyUser1);
        });


        //查询会员并转移
        User user = new User();
        user.setSecondProxy(secondProxy.getId());
        List<User> userList = userService.findUserList(user, null, null);
        if (userList != null || userList.size() >= CommonConst.NUMBER_1){
            log.info("转移代理被转移者id{} 会员数量{}",proxyUser.getSecondProxy(),userList.size());
            userList.forEach(user1 -> {
                user1.setFirstProxy(firstProxy.getId());
                userService.save(user1);
            });
            userList.clear();
        }

        proxyUserService.subProxyUsersNum(secondProxy.getFirstProxy(),1);
        proxyUserService.addProxyUsersNum(firstProxy.getId(),1);
    }
}
