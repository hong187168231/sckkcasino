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
@Api(tags = "公告管理")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;
    /**
     * 新增公告
     * @param title 公告内容
     * @param isShelves 是否上架 是true 否false
     * @param url 详情访问页
     * @param introduction 简介
     * @return
     */
    @ApiOperation("新增公告/活动")
    @PostMapping("/saveNotice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "title", value = "公告内容", required = true),
            @ApiImplicitParam(name = "isShelves", value = "是否上架 是true 否false", required = true),
            @ApiImplicitParam(name = "url", value = "详情访问页", required = true),
            @ApiImplicitParam(name = "introduction", value = "简介", required = true),
    })
    public ResponseEntity<Notice> saveNotice(String title,Boolean isShelves,String introduction,String url){
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setIntroduction(introduction);
        notice.setIsShelves(isShelves);
        notice.setUrl(url);
        notice.setUpdateTime(new Date());
        notice.setCreateTime(new Date());
        return this.saveNotice(notice);
    }

    private synchronized ResponseEntity<Notice> saveNotice(Notice notice){
        List<Notice> byNoticeList = noticeService.findByNoticeList();
        if (byNoticeList != null && byNoticeList.size() >= CommonConst.NUMBER_10){
            return ResponseUtil.custom(CommonConst.THENUMBERISLIMITEDTO10);
        }
        Notice no = noticeService.saveNotice(notice);
        return ResponseUtil.success(no);
    }

    @ApiOperation("上架下架活动/公告")
    @GetMapping("/deleteNotice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id主键", required = true),
    })
    public ResponseEntity<Notice> deleteNotice(Long id){
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
        Notice no = noticeService.saveNotice(notice);
        return ResponseUtil.success(no);
    }
    /**
     * 修改公告
     * @param id 公告id
     * @param title 公告内容
     * @param isShelves 是否上架 是true 否false
     * @param url 详情访问页url
     * @param introduction 简介
     * @return
     */
    @ApiOperation("修改公告/活动")
    @PostMapping("/updateNotice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "公告id", required = true),
            @ApiImplicitParam(name = "title", value = "公告内容", required = true),
            @ApiImplicitParam(name = "isShelves", value = "是否上架 是true 否false", required = true),
            @ApiImplicitParam(name = "url", value = "详情访问页url", required = true),
            @ApiImplicitParam(name = "introduction", value = "简介", required = true),
    })
    public ResponseEntity<Notice> updateNotice(String title,Boolean isShelves,String introduction,String url,Long id){
        Notice notice = noticeService.findNoticeById(id);
        if (notice == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
        notice.setUrl(url);
        notice.setIsShelves(isShelves);
        notice.setIntroduction(introduction);
        notice.setTitle(title);
        notice.setUpdateTime(new Date());
        Notice no = noticeService.saveNotice(notice);
        return ResponseUtil.success(no);
    }
    /**
     * 查询所有 最多十条
     * @return
     */
    @ApiOperation("查询公告/活动")
    @GetMapping("/findNotice")
    public ResponseEntity<Notice> findNotice(){
        List<Notice> noticeList = noticeService.findByNoticeList();
        return new ResponseEntity(ResponseCode.SUCCESS, noticeList);
    }
}
