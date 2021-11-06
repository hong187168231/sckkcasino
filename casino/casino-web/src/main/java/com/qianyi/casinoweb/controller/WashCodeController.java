package com.qianyi.casinoweb.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.WashCodeVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.*;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("washCode")
@Api(tags = "洗码")
public class WashCodeController {

    @Autowired
    private WashCodeConfigService washCodeConfigService;
    @Autowired
    private UserWashCodeConfigService userWashCodeConfigService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private WashCodeChangeService washCodeChangeService;
    @Autowired
    @Qualifier("accountChangeJob")
    AsyncService asyncService;


    @ApiOperation("用户洗码列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "时间：0：今天，1：昨天，2：近7天", required = true)
    })
    @GetMapping("/getList")
    public ResponseEntity<ChargeOrderListData> chargeOrderList(String date) {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        String startTime = null;
        String endTime = null;
        if ("1".equals(date)) {
            startTime = DateUtil.getStartTime(-1);
            endTime = DateUtil.getEndTime(-1);
        } else if ("2".equals(date)) {
            startTime = DateUtil.getStartTime(-7);
            endTime = DateUtil.getEndTime(0);
        } else {//默认查今天
            startTime = DateUtil.getStartTime(0);
            endTime = DateUtil.getEndTime(0);
        }
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        BigDecimal washCode = BigDecimal.ZERO.setScale(2);;
        if (userMoney != null && userMoney.getWashCode() != null) {
            washCode = userMoney.getWashCode();
        }
        List<WashCodeConfig> washCodeConfig = userWashCodeConfigService.getWashCodeConfig(Constants.PLATFORM,userId);
        List<WashCodeChange> list = washCodeChangeService.getList(userId, startTime, endTime);
        ChargeOrderListData chargeOrderListData=new ChargeOrderListData();
        List<WashCodeVo> voList = new ArrayList<>();
        //洗码比例取配置表的,数据为空返回默认值
        if (CollectionUtils.isEmpty(list)) {
            WashCodeVo washCodeVo = null;
            for (WashCodeConfig config : washCodeConfig) {
                washCodeVo = new WashCodeVo();
                BeanUtils.copyProperties(config, washCodeVo);
                washCodeVo.setValidbet(BigDecimal.ZERO);
                washCodeVo.setRate(config.getRate() + "%");
                washCodeVo.setAmount(BigDecimal.ZERO);
                voList.add(washCodeVo);
            }
            chargeOrderListData.setTotalAmount(washCode);
            chargeOrderListData.setList(voList);
            return ResponseUtil.success(chargeOrderListData);
        }
        WashCodeVo washCodeVo = null;
        for (WashCodeConfig config : washCodeConfig) {
            washCodeVo = new WashCodeVo();
            boolean flag = true;
            for (WashCodeChange change : list) {
                if (!ObjectUtils.isEmpty(config.getGameId()) && config.getGameId().equals(change.getGameId())) {
                    BeanUtils.copyProperties(change, washCodeVo);
                    washCodeVo.setRate(config.getRate() + "%");
                    washCodeVo.setGameName(config.getGameName());
                    voList.add(washCodeVo);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                BeanUtils.copyProperties(config, washCodeVo);
                washCodeVo.setValidbet(BigDecimal.ZERO);
                washCodeVo.setRate(config.getRate() + "%");
                washCodeVo.setAmount(BigDecimal.ZERO);
                voList.add(washCodeVo);
            }
        }
        chargeOrderListData.setTotalAmount(washCode);
        chargeOrderListData.setList(voList);
        return ResponseUtil.success(chargeOrderListData);
    }

    @ApiOperation("用户领取洗码")
    @GetMapping("/receiveWashCode")
    @Transactional
    public ResponseEntity<String> receiveWashCode() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        BigDecimal washCode = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getWashCode() != null) {
            washCode = userMoney.getWashCode();
        }
        if (washCode.compareTo(BigDecimal.ONE) == -1) {
            return ResponseUtil.custom("金额小于1,不能领取");
        }
        userMoneyService.addMoney(userId, washCode);
        userMoneyService.subWashCode(userId, washCode);

        AccountChangeVo vo=new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.WASH_CODE);
        vo.setAmount(washCode);
        vo.setAmountBefore(userMoney.getMoney());
        vo.setAmountAfter(userMoney.getMoney().add(washCode));
        asyncService.executeAsync(vo);
        return ResponseUtil.success("成功领取金额：" + washCode);
    }

    @Data
    @ApiModel("用户洗码列表")
    class ChargeOrderListData{
        @ApiModelProperty(value = "洗码金额")
        @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
        private BigDecimal totalAmount;

        @ApiModelProperty(value = "数据列表")
        private List<WashCodeVo> list;
    }
}
