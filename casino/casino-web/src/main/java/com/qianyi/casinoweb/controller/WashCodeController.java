package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.UserWashCodeConfig;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserWashCodeConfigService;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.WashCodeVo;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private RedisUtil redisUtil;

    @ApiOperation("用户洗码列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "时间：全部：不传值，0：今天，1：昨天，2：近7天", required = false)
    })
    @GetMapping("/getList")
    public ResponseEntity chargeOrderList(String date) {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        List<String> dateList = new ArrayList<>();
        if ("0".equals(date)) {
            dateList.add(getDate(0));
        } else if ("1".equals(date)) {
            dateList.add(getDate(-1));
        } else if ("2".equals(date)) {
            for (int i = 0; i > -7; i--) {
                dateList.add(getDate(i));
            }
        }

        Map<String, Object> data = new HashMap<>();
        List<WashCodeVo> list = new ArrayList<>();
        String platform = "wm";
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<UserWashCodeConfig> codeConfigs = userWashCodeConfigService.findByUserIdAndPlatform(userId, platform);
        if (!CollectionUtils.isEmpty(codeConfigs)) {
            for (UserWashCodeConfig codeConfig : codeConfigs) {
                WashCodeVo washCodeVo = setData(userId, dateList, codeConfig.getGameId(), codeConfig);
                list.add(washCodeVo);
                totalAmount = totalAmount.add(washCodeVo.getAmount());
            }
            data.put("totalAmount", totalAmount);
            data.put("list", list);
            return ResponseUtil.success(data);
        }
        List<WashCodeConfig> configs = washCodeConfigService.findByPlatform(platform);
        if (!CollectionUtils.isEmpty(configs)) {
            for (WashCodeConfig codeConfig : configs) {
                WashCodeVo washCodeVo = setData(userId, dateList, codeConfig.getGameId(), codeConfig);
                list.add(washCodeVo);
                totalAmount = totalAmount.add(washCodeVo.getAmount());
            }
        }
        data.put("totalAmount", totalAmount);
        data.put("list", list);
        return ResponseUtil.success(data);
    }

    private WashCodeVo setData(Long userId, List<String> dateList, String gameId, Object codeConfig) {
        WashCodeVo vo = new WashCodeVo();
        BeanUtils.copyProperties(codeConfig, vo);
        vo.setAmount(BigDecimal.ZERO);
        vo.setValidbet(BigDecimal.ZERO);
        //查询全部
        if(CollectionUtils.isEmpty(dateList)){
            String prex = "wm:" + userId + ":" + gameId + ":*";
            Set<String> keys = redisUtil.getKeysByPrex(prex);
            if(!CollectionUtils.isEmpty(keys)){
                for (String key:keys){
                    Object val = redisUtil.get(key);
                    if (!ObjectUtils.isEmpty(val)) {
                        WashCodeVo codeVo = (WashCodeVo) val;
                        vo.setAmount(vo.getAmount().add(codeVo.getAmount()));
                        vo.setValidbet(vo.getValidbet().add(codeVo.getValidbet()));
                    }
                }
            }
            return vo;
        }
        //根据条件查询
        for (String date : dateList) {
            String key = "wm:" + userId + ":" + gameId + ":" + date;
            Object val = redisUtil.get(key);
            if (!ObjectUtils.isEmpty(val)) {
                WashCodeVo codeVo = (WashCodeVo) val;
                vo.setAmount(vo.getAmount().add(codeVo.getAmount()));
                vo.setValidbet(vo.getValidbet().add(codeVo.getValidbet()));
            }
        }
        return vo;
    }

    private static String getDate(int num) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.YYYYMMDD);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, num);
        String date = sdf.format(calendar.getTime());
        return date;
    }

    @ApiOperation("用户领取洗码")
    @GetMapping("/receiveWashCode")
    @Transactional
    public ResponseEntity receiveWashCode() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        String prex = "wm:" + userId + ":*";
        Set<String> keys = redisUtil.getKeysByPrex(prex);
        BigDecimal totalAmount=BigDecimal.ZERO;
        if(!CollectionUtils.isEmpty(keys)) {
            for (String key : keys) {
                Object val = redisUtil.get(key);
                if (!ObjectUtils.isEmpty(val)) {
                    WashCodeVo codeVo = (WashCodeVo) val;
                    totalAmount=totalAmount.add(codeVo.getAmount());
                }
            }
            userMoneyService.findUserByUserIdUseLock(userId);
            userMoneyService.addMoney(userId,totalAmount);
            redisUtil.deleteByPrex(prex);
        }
        return ResponseUtil.success();
    }
}
