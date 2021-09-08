//package com.qianyi.casinocore.repository;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.Date;
//
//@SpringBootTest
//@RunWith(SpringJUnit4ClassRunner.class)
//public class BankcardsRepositoryTest {
//
//    @Autowired
//    BankcardsRepository bankcardsRepository;
//
//    @Test
//    public void should_save_banck(){
//
//        Date now = new Date();
//        Bankcards bankcards = new Bankcards();
//        bankcards.setUserId(1l);
//        bankcards.setBankId(1l);
//        bankcards.setBankAccount("bankAccount");
//        bankcards.setAddress("address");
//        bankcards.setRealName("jjjj");
//        bankcards.setUpdateTime(now);
//        bankcards.setCreateTime(now);
//        bankcards.setDisable(0);
//        bankcards.setDefaultCard(1);
//        bankcardsRepository.save(bankcards);
//
//    }
//
//}