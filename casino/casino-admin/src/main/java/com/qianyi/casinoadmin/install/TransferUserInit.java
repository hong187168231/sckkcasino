//package com.qianyi.casinoadmin.install;
//
//import com.qianyi.casinoadmin.util.LoginUtil;
//import com.qianyi.casinocore.model.ProxyUser;
//import com.qianyi.casinocore.model.User;
//import com.qianyi.casinocore.service.ProxyUserService;
//import com.qianyi.casinocore.service.UserService;
//import com.qianyi.casinocore.util.CommonConst;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//@Slf4j
//@Order(value = 8)
//public class TransferUserInit implements CommandLineRunner {
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private ProxyUserService proxyUserService;
//
//    private static List<String> users = new ArrayList<>();
//
//    static {
//        users.add("povpov188");
////        users.add("Rahu855");
////        users.add("Makara911");
////        users.add("Kimhong009");
////        users.add("VANNDA33");
////        users.add("MNUSSMOS33");
////        users.add("HAILENG18");
////        users.add("pozzmab186");
////        users.add("NAKA99");
////        users.add("PHALLARA555");
////        users.add("Dornty888");
////        users.add("BONGPOV168");
////        users.add("Chhay888");
////        users.add("kimsreang522");
////        users.add("HUYVANARITH55");
////        users.add("ChanChan003");
////        users.add("MrrHout001");
////        users.add("Dano9595");
////        users.add("NopNop002");
////        users.add("Rith168");
////        users.add("BongNang777");
////        users.add("HeaTen002");
////        users.add("Neang999");
////        users.add("TeTe777");
////        users.add("Bongda777");
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        log.info("初始化转移会员开始============================================》");
//        ProxyUser d018JD = proxyUserService.findByUserName("D018JD");
//        if (LoginUtil.checkNull(d018JD)){
//            log.info("没有找到这个代理转移结束");
//            return;
//        }
//        if (d018JD.getProxyRole() != CommonConst.NUMBER_3){
//            log.info("只有基层代理可以转移会员");
//            return;
//        }
//        for (String str:users){
//            User byAccount = userService.findByAccount(str);
//            if (LoginUtil.checkNull(byAccount)){
//                log.info("没有找到这个会员{}",str);
//                continue;
//            }
//            log.info("被转移会员账号{}总代{}区代{}基代{}",byAccount.getAccount(),byAccount.getFirstProxy(),byAccount.getSecondProxy(),byAccount.getThirdProxy());
//            byAccount.setFirstProxy(d018JD.getFirstProxy());
//            byAccount.setSecondProxy(d018JD.getSecondProxy());
//            byAccount.setThirdProxy(d018JD.getId());
//            userService.save(byAccount);
//            log.info("转移之后会员账号{}总代{}区代{}基代{}",byAccount.getAccount(),byAccount.getFirstProxy(),byAccount.getSecondProxy(),byAccount.getThirdProxy());
//        }
//        log.info("初始化转移会员结束============================================》");
//    }
//}
