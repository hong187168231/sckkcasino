package com.qianyi.casinoreport.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinoreport.vo.GoldenFTimeVO;
import com.qianyi.lottery.util.StringUtils;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(tags = "手动补单")
@Component
@Slf4j
public class SupplementPgController {

    @Autowired
    private GameRecordGoldenFJob gameRecordGoldenFJob;
    @Autowired
    private RedisUtil redisUtil;

    @Scheduled(initialDelay = 7000, fixedDelay = 1000 * 60 * 2)
    public void pullGoldenF_PGBD() throws ParseException {
        log.info("定时器开始拉取PG真人注单记录");
        String startTime = (String)redisUtil.get("PG:repair:startTime");
        String endTime = (String)redisUtil.get("PG:repair:endTime");
        String secretkey = (String)redisUtil.get("PG:repair:secretkey");
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            if (StringUtils.isNotBlank(secretkey) && secretkey.equals("puff520miyao")) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                goldenFSupplement(Constants.PLATFORM_CQ9,  df.parse(startTime).getTime(), df.parse(endTime).getTime());
            }
        }
    }

    @Scheduled(initialDelay = 7000, fixedDelay = 1000 * 60 * 3)
    public void pullGoldenF_PGBD2() throws ParseException {
        log.info("定时器开始拉取PG2真人注单记录");
        String startTime = (String)redisUtil.get("PG2:repair:startTime");
        String endTime = (String)redisUtil.get("PG2:repair:endTime");
        String secretkey = (String)redisUtil.get("PG2:repair:secretkey");
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            if (StringUtils.isNotBlank(secretkey) && secretkey.equals("puff520miyao")) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                goldenFSupplement2(Constants.PLATFORM_CQ9,  df.parse(startTime).getTime(), df.parse(endTime).getTime());
            }
        }
    }

    public void goldenFSupplement(String vendorCode, Long startTime, Long endTime) {
        redisUtil.set("PG:repair:secretkey", "1");
        if (!Constants.PLATFORM_PG.equals(vendorCode) && !Constants.PLATFORM_CQ9.equals(
            vendorCode) && !Constants.PLATFORM_SABASPORT.equals(vendorCode)) {
            log.error("产品代码错误");
            return;
        }
        if (startTime.toString().length() != 13 || endTime.toString().length() != 13) {

            log.error("时间格式错误");
            return;
        }
        List<GoldenFTimeVO> timeVOS = new ArrayList<>();
        log.info("开始时间:{},结束时间:{}", startTime, endTime);
        Long range = endTime - startTime;
        if (range <= 0) {
            log.error("结束时间大于开始时间");
            return;
        }
        long diff = new Date().getTime() - startTime;
        //WM报表资料只保留60天。
        long days = diff / (1000 * 60 * 60 * 24);
        if (days > 60) {
            log.error("仅允许补单60天内的数据");
            return;
        }
        log.info("{}", range);
        Long num = range / (4 * 60 * 1000);
        log.info("num is {}", num);
        for (int i = 0; i <= num; i++) {
            startTime = startTime - 60 * 1000;//每次拉取重叠一分钟
            GoldenFTimeVO goldenFTimeVO = new GoldenFTimeVO();
            Long tempEndTime = startTime + (5 * 60 * 1000);
            goldenFTimeVO.setStartTime(startTime);
            goldenFTimeVO.setEndTime(tempEndTime > endTime ? endTime : tempEndTime);
            timeVOS.add(goldenFTimeVO);
            startTime = tempEndTime;
        }
        log.info("{}", timeVOS);
        gameRecordGoldenFJob.supplementPullGameRecord(vendorCode, timeVOS);
        log.info("{}补单完成,timeList={}", vendorCode, JSON.toJSONString(timeVOS));
        redisUtil.set("PG:repair:secretkey", "puff520miyao");
    }


    public void goldenFSupplement2(String vendorCode, Long startTime, Long endTime) {
        redisUtil.set("PG2:repair:secretkey", "1");
        if (!Constants.PLATFORM_PG.equals(vendorCode) && !Constants.PLATFORM_CQ9.equals(
            vendorCode) && !Constants.PLATFORM_SABASPORT.equals(vendorCode)) {
            log.error("产品代码错误");
            return;
        }
        if (startTime.toString().length() != 13 || endTime.toString().length() != 13) {

            log.error("时间格式错误");
            return;
        }
        List<GoldenFTimeVO> timeVOS = new ArrayList<>();
        log.info("开始时间:{},结束时间:{}", startTime, endTime);
        Long range = endTime - startTime;
        if (range <= 0) {
            log.error("结束时间大于开始时间");
            return;
        }
        long diff = new Date().getTime() - startTime;
        //WM报表资料只保留60天。
        long days = diff / (1000 * 60 * 60 * 24);
        if (days > 60) {
            log.error("仅允许补单60天内的数据");
            return;
        }
        log.info("{}", range);
        Long num = range / (4 * 60 * 1000);
        log.info("num is {}", num);
        for (int i = 0; i <= num; i++) {
            startTime = startTime - 60 * 1000;//每次拉取重叠一分钟
            GoldenFTimeVO goldenFTimeVO = new GoldenFTimeVO();
            Long tempEndTime = startTime + (5 * 60 * 1000);
            goldenFTimeVO.setStartTime(startTime);
            goldenFTimeVO.setEndTime(tempEndTime > endTime ? endTime : tempEndTime);
            timeVOS.add(goldenFTimeVO);
            startTime = tempEndTime;
        }
        log.info("{}", timeVOS);
        gameRecordGoldenFJob.supplementPullGameRecord(vendorCode, timeVOS);
        log.info("{}补单完成2,timeList={}", vendorCode, JSON.toJSONString(timeVOS));
        redisUtil.set("PG2:repair:secretkey", "puff520miyao");
    }
}
