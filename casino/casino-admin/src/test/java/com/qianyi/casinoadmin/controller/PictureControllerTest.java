package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class PictureControllerTest {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;


    @Test
    public void findproxy(){
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        System.out.println(proxyUserList.size());
    }

    @Test
    public void find() throws ParseException {
        User user = new User();
        String startTime = "2021-11-24 00:00:00";
        String endTime = "2021-11-24 23:59:59";
        Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
        Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
        List<User> userList = userService.findUserList(user, startDate, endDate);
        System.out.println(userList);
        Long userCount = userService.findUserCount(user, startDate, endDate);
        Integer count = Math.toIntExact(userCount);
        System.out.println(count);

    }

    @Test
    public void should_cache_all_pic(){
        pictureService.findAll();
    }

    @Test
    public void should_cache_conidtion_pic(){
        List<LunboPic> lunboPicList = pictureService.findByTheShowEnd(2);
        System.out.println(lunboPicList.size());
    }

    @Test
    public void should_create_pic(){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setNo(123);
        lunboPic.setRemark("123");
        lunboPic.setUrl("//123//123");
        pictureService.save(lunboPic);
    }

}