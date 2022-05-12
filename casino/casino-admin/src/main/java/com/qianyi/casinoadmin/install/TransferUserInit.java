package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Order(value = 8)
public class TransferUserInit implements CommandLineRunner {
    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    private static List<String> users1 = new ArrayList<>();
    private static String proxyName1 = "xiaojieJD";

    static {
        users1.add("DH6666");
        users1.add("SENGLY5555");
        users1.add("Sarun5555");
        users1.add("Samnang2022");
//        users1.add("ahyokh168");
//        users1.add("bongvina005");
    }

    private static List<String> users2 = new ArrayList<>();
    private static String proxyName2 = "yadaJD";
    static {
        users2.add("HokRatha7878");
        users2.add("SENG99");
        users2.add("volvo888");
        users2.add("coco168");
//        users2.add("ahyokh168");
//        users1.add("bongvina005");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("初始化转移会员开始============================================》");
        transferUser(users1,proxyName1);
        transferUser(users2,proxyName2);
        log.info("初始化转移会员结束============================================》");
    }

    private void transferUser(List<String> users,String proxyName){
        ProxyUser proxyUser = proxyUserService.findByUserName(proxyName);
        if (LoginUtil.checkNull(proxyUser)){
            log.info("没有找到这个代理转移结束{}",proxyName);
            return;
        }
        if (proxyUser.getProxyRole() != CommonConst.NUMBER_3){
            log.info("只有基层代理可以转移会员");
            return;
        }
        for (String str:users){
            User byAccount = userService.findByAccount(str);
            if (LoginUtil.checkNull(byAccount)){
                log.info("没有找到这个会员{}",str);
                continue;
            }
            log.info("被转移会员账号{}总代{}区代{}基代{}",byAccount.getAccount(),byAccount.getFirstProxy(),byAccount.getSecondProxy(),byAccount.getThirdProxy());
            byAccount.setFirstProxy(proxyUser.getFirstProxy());
            byAccount.setSecondProxy(proxyUser.getSecondProxy());
            byAccount.setThirdProxy(proxyUser.getId());
            userService.save(byAccount);
            log.info("转移之后会员账号{}总代{}区代{}基代{}",byAccount.getAccount(),byAccount.getFirstProxy(),byAccount.getSecondProxy(),byAccount.getThirdProxy());
        }
    }
}
