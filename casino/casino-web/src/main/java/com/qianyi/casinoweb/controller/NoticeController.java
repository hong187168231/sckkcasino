package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.Notice;
import com.qianyi.casinocore.service.NoticeService;
import com.qianyi.casinoweb.vo.NoticeVo;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
    public ResponseEntity<List<NoticeVo>> newest() {
        List<Notice> list = noticeService.newest();
        List<NoticeVo> voList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return ResponseUtil.success(voList);
        }
        //前端时间要求去掉秒
        NoticeVo vo = null;
        for (Notice notice : list) {
            vo = new NoticeVo();
            BeanUtils.copyProperties(notice, vo);
            voList.add(vo);
        }
        return ResponseUtil.success(voList);
    }
}
