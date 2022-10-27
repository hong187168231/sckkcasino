//package com.qianyi.casinoadmin.install;
//
//import com.qianyi.casinoadmin.util.LoginUtil;
//import com.qianyi.casinocore.model.BankInfo;
//import com.qianyi.casinocore.service.BankInfoService;
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
//@Order(12)
//public class BankInfoInitialization implements CommandLineRunner {
//
//    @Autowired
//    private BankInfoService bankInfoService;
//
//    public static List<Long> list = new ArrayList<>();
//
//    static {
//        list.add(199L);
//        list.add(201L);
//        list.add(215L);
//        list.add(217L);
//        list.add(219L);
//        list.add(221L);
//        list.add(225L);
//        list.add(227L);
//        list.add(229L);
//        list.add(231L);
//        list.add(233L);
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        for (Long id:list){
//            BankInfo byId = bankInfoService.findById(id);
//            if (LoginUtil.checkNull(byId)){
//                continue;
//            }
//            bankInfoService.saveBankInfo(byId);
//        }
//    }
//}
