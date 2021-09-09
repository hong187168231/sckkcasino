package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinocore.model.Marquee;
import com.qianyi.casinocore.service.MarqueeService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/marquee")
@Api(tags = "跑马灯设置")
public class MarqueeController {
    @Autowired
    private MarqueeService marqueeService;

    @ApiOperation("新增跑马灯")
    @PostMapping("/saveMarquee")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "content", value = "内容", required = true),
            @ApiImplicitParam(name = "state", value = "状态 1 启用 0 停用", required = true),
    })
    public ResponseEntity saveMarquee(String content,Integer state){
        Marquee marquee = new Marquee();
        marquee.setState(state);
        marquee.setContent(content);
        marquee.setUpdateTime(new Date());
        return this.savePicture(marquee);
    }

    private synchronized ResponseEntity savePicture(Marquee marquee){
        List<Marquee> byMarqueeList = marqueeService.findByMarqueeList();
        if (byMarqueeList != null && byMarqueeList.size() >= CommonConst.NUMBER_10){
            return ResponseUtil.custom(CommonConst.THENUMBERISLIMITEDTO10);
        }
        marqueeService.saveMarquee(marquee);
        return ResponseUtil.success();
    }
    @ApiOperation("删除跑马灯")
    @GetMapping("/deleteMarquee")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id主键", required = true),
    })
    public ResponseEntity deleteMarquee(Long id){
        marqueeService.deleteById(id);
        return ResponseUtil.success();
    }
    @ApiOperation("修改跑马灯")
    @PostMapping("/updateMarquee")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id主键", required = true),
            @ApiImplicitParam(name = "content", value = "内容", required = true),
            @ApiImplicitParam(name = "state", value = "状态 1 启用 0 停用", required = true),
    })
    public ResponseEntity updateMarquee(String content,Integer state,Long id){
        Marquee marquee = new Marquee();
        marquee.setState(state);
        marquee.setContent(content);
        marquee.setId(id);
        marquee.setUpdateTime(new Date());
        marqueeService.saveMarquee(marquee);
        return ResponseUtil.success();
    }

    @ApiOperation("查询跑马灯")
    @GetMapping("/findMarquee")
    public ResponseEntity findMarquee(){
        List<Marquee> byMarqueeList = marqueeService.findByMarqueeList();
        return ResponseUtil.success(byMarqueeList);
    }
}
