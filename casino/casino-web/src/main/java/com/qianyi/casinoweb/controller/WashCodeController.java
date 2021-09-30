package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.WashCodeVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
    public ResponseEntity chargeOrderList(String date) {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        List<String> dateList = new ArrayList<>();
        String startTime = null;
        String endTime = null;
        if ("0".equals(date)) {
            startTime = DateUtil.getStartTime(0);
            endTime = DateUtil.getEndTime(0);
        } else if ("1".equals(date)) {
            startTime = DateUtil.getStartTime(-1);
            endTime = DateUtil.getEndTime(-1);
        } else if ("2".equals(date)) {
            startTime = DateUtil.getStartTime(-7);
            endTime = DateUtil.getEndTime(0);
        } else {
            return ResponseUtil.custom("date值仅限于0,1,2");
        }
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        BigDecimal washCode= userMoney.getWashCode() == null ? BigDecimal.ZERO : userMoney.getWashCode().setScale(2, BigDecimal.ROUND_HALF_UP);
        List<WashCodeConfig> washCodeConfig = getWashCodeConfig(userId);
        List<WashCodeChange> list = washCodeChangeService.getList(userId, startTime, endTime);
        Map<String, Object> data = new HashMap<>();
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
            data.put("totalAmount", washCode);
            data.put("list", voList);
            return ResponseUtil.success(data);
        }
        for (WashCodeConfig config : washCodeConfig) {
            WashCodeVo washCodeVo = null;
            for (WashCodeChange change : list) {
                washCodeVo = new WashCodeVo();
                if (!ObjectUtils.isEmpty(config.getGameId()) && config.getGameId().equals(change.getGameId())) {
                    BeanUtils.copyProperties(change, washCodeVo);
                    washCodeVo.setRate(config.getRate() + "%");
                    voList.add(washCodeVo);
                    break;
                } else {
                    BeanUtils.copyProperties(config, washCodeVo);
                    washCodeVo.setValidbet(BigDecimal.ZERO);
                    washCodeVo.setRate(config.getRate() + "%");
                    voList.add(washCodeVo);
                    break;
                }
            }
        }
        data.put("totalAmount", washCode);
        data.put("list", voList);
        return ResponseUtil.success(data);
    }

    private List<WashCodeConfig> getWashCodeConfig(Long userId) {
        List<UserWashCodeConfig> codeConfigs = userWashCodeConfigService.findByUserIdAndPlatform(userId, Constants.PLATFORM);
        if (!CollectionUtils.isEmpty(codeConfigs)) {
            List<WashCodeConfig> list = new ArrayList<>();
            WashCodeConfig config = null;
            for (UserWashCodeConfig codeConfig : codeConfigs) {
                config = new WashCodeConfig();
                BeanUtils.copyProperties(codeConfig, config);
            }
            return list;
        }
        List<WashCodeConfig> configs = washCodeConfigService.findByPlatform(Constants.PLATFORM);
        return configs;
    }

    @ApiOperation("用户领取洗码")
    @GetMapping("/receiveWashCode")
    @Transactional
    public ResponseEntity receiveWashCode() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney.getWashCode() == null) {
            userMoney.setWashCode(BigDecimal.ZERO);
        }
        if (userMoney.getWashCode().compareTo(BigDecimal.ZERO) == 0) {
            return ResponseUtil.custom("洗码金额为0");
        }
        userMoneyService.addMoney(userId, userMoney.getWashCode());
        userMoneyService.subWashCode(userId, userMoney.getWashCode());

        AccountChangeVo vo=new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.WASH_CODE);
        vo.setAmount(userMoney.getWashCode());
        vo.setAmountBefore(userMoney.getMoney());
        vo.setAmountAfter(userMoney.getMoney().add(userMoney.getWashCode()));
        asyncService.executeAsync(vo);
        return ResponseUtil.success();
    }
}
