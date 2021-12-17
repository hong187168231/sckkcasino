package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinoweb.job.GameRecordAsyncOper;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Api(tags = "测试")
@RestController
@RequestMapping("test")
@Slf4j
public class TestController {

    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;
    @Autowired
    private GameRecordService gameRecordService;
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("sendMq")
    @ApiOperation("批量发送分润MQ")
    @NoAuthentication
    @ApiImplicitParam(name = "id", value = "起始ID ", required = true)
    public ResponseEntity sendMq(Long id) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = format.format(new Date());
        List<GameRecord> list = gameRecordService.findByCreateByAndIdGreaterThanEqualOrderByIdAsc("0", id);
        redisUtil.set("sendMq::startTime::" + id, startTime);
        for (GameRecord gameRecord : list) {
            gameRecordAsyncOper.shareProfit(gameRecord);
            redisUtil.set("sendMq::endId::" + id, gameRecord.getId());
            redisUtil.incr("sendMq::totalNum", 1);
        }
        String endTime = format.format(new Date());
        redisUtil.set("sendMq::endTime::" + id, endTime);
        return ResponseUtil.success();
    }
}
