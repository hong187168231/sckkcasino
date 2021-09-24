package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.LunboPic;
import com.qianyi.casinocore.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class PictureControllerTest {

    @Autowired
    private PictureService pictureService;


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