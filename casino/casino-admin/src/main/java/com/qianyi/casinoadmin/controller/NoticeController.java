package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinocore.model.Notice;
import com.qianyi.casinocore.service.NoticeService;
import com.qianyi.modulecommon.reponse.ResponseCode;
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
@RequestMapping("/notice")
@Api(tags = "公告中心")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @ApiOperation("新增公告")
    @PostMapping("/saveNotice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "title", value = "内容", required = true),
            @ApiImplicitParam(name = "isShelves", value = "是否上架 true false", required = true),
            @ApiImplicitParam(name = "url", value = "详情访问页", required = true),
            @ApiImplicitParam(name = "introduction", value = "简介", required = true),
    })
    public ResponseEntity saveNotice(String title,Boolean isShelves,String introduction,String url){
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setIntroduction(introduction);
        notice.setIsShelves(isShelves);
        notice.setUrl(url);
        return this.saveNotice(notice);
    }

    private synchronized ResponseEntity saveNotice(Notice notice){
        List<Notice> byNoticeList = noticeService.findByNoticeList();
        if (byNoticeList != null && byNoticeList.size() >= CommonConst.NUMBER_10){
            return ResponseUtil.custom(CommonConst.THENUMBERISLIMITEDTO10);
        }
        noticeService. saveNotice(notice);
        return ResponseUtil.success();
    }
    @ApiOperation("上架下架活动")
    @GetMapping("/deleteNotice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id主键", required = true),
    })
    public ResponseEntity deleteNotice(Long id){
        Notice notice = noticeService.findNoticeById(id);
        if(notice == null){
            return ResponseUtil.custom("不存在该活动");
        }
        boolean isShelves = notice.getIsShelves();
        if(isShelves == true){
            notice.setIsShelves(false);
        }else{
            notice.setIsShelves(true);
        }
        noticeService.saveNotice(notice);
        return ResponseUtil.success();
    }
    @ApiOperation("修改公告")
    @PostMapping("/updateNotice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id主键", required = true),
            @ApiImplicitParam(name = "title", value = "内容", required = true),
            @ApiImplicitParam(name = "isShelves", value = "是否上架 true false", required = true),
            @ApiImplicitParam(name = "url", value = "详情访问页", required = true),
            @ApiImplicitParam(name = "introduction", value = "简介", required = true),
    })
    public ResponseEntity updateNotice(String title,Boolean isShelves,String introduction,String url,Long id){
        Notice notice = noticeService.findNoticeById(id);
        if (notice == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
        notice.setUrl(url);
        notice.setIsShelves(isShelves);
        notice.setIntroduction(introduction);
        notice.setTitle(title);
        noticeService.saveNotice(notice);
        return ResponseUtil.success();
    }

    @ApiOperation("查询所有")
    @GetMapping("/findNotice")
    public ResponseEntity findNotice(){
        List<Notice> noticeList = noticeService.findByNoticeList();
        return new ResponseEntity(ResponseCode.SUCCESS, noticeList);
    }
}
