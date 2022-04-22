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

    private static List<String> users = new ArrayList<>();

    static {
        users.add("poypoyd31");
        users.add("kesthai001");
        users.add("Kimyun12");
        users.add("Sethseth");
        users.add("ahyokh168");
        users.add("bongvina005");
        users.add("BONGVIREAK007");
        users.add("bongdina001");
        users.add("Heaseng123");
        users.add("bongdara123");
        users.add("Honbina12");
        users.add("Chanseng12");
        users.add("Dyna12");
        users.add("RKRK12");
        users.add("chandara123");
        users.add("MrrTra002");
        users.add("BONGCHEAT123");
        users.add("bongdom123");
        users.add("bongvanna123");
        users.add("kinggame123");
        users.add("bongny168");
        users.add("bongkimsan123");
        users.add("bongveng168");
        users.add("kaochin168");
        users.add("MRJHON123");
        users.add("MRDAWIN168");
        users.add("jovyunfat123");
        //        users.add("Bongda777");

        //        users.add("Bongda777");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("初始化转移会员开始============================================》");
        ProxyUser d018JD = proxyUserService.findByUserName("D031JD");
        if (LoginUtil.checkNull(d018JD)){
            log.info("没有找到这个代理转移结束");
            return;
        }
        if (d018JD.getProxyRole() != CommonConst.NUMBER_3){
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
            byAccount.setFirstProxy(d018JD.getFirstProxy());
            byAccount.setSecondProxy(d018JD.getSecondProxy());
            byAccount.setThirdProxy(d018JD.getId());
            userService.save(byAccount);
            log.info("转移之后会员账号{}总代{}区代{}基代{}",byAccount.getAccount(),byAccount.getFirstProxy(),byAccount.getSecondProxy(),byAccount.getThirdProxy());
        }
        log.info("初始化转移会员结束============================================》");
    }
}
