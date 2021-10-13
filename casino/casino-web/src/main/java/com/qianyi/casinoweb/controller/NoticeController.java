package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.Notice;
import com.qianyi.casinocore.service.NoticeService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("notice")
@Api(tags = "公告中心")
public class NoticeController {

    @Autowired
    NoticeService noticeService;

    @GetMapping("newest")
    @ApiOperation("公告/活动")
    @NoAuthentication
    public ResponseEntity<Notice> newest() {
        List<Notice> list = noticeService.newest();
        return ResponseUtil.success(list);
    }
}
