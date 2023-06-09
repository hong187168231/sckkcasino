package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.business.ChargeBusiness;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.CollectionBankcardService;
import com.qianyi.casinocore.util.RedisLockUtil;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.CollectionBankcardVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/charge")
@Slf4j
@Api(tags = "银行卡线下充值")
public class ChargeController {

    @Autowired
    private ChargeBusiness chargeBusiness;
    @Autowired
    private CollectionBankcardService collectionBankcardService;
    @Autowired
    private BankInfoService bankInfoService;
    @Autowired
    private RedisLockUtil redisLockUtil;


    @GetMapping("/collect_bankcards")
    @ApiOperation("收款银行卡列表")
    @ResponseBody
    public ResponseEntity<List<CollectionBankcardVo>> bankList() {
        List<CollectionBankcard> bankcardList = collectionBankcardService.findByDisableOrderBySortIdAsc(0);
        if (CollectionUtils.isEmpty(bankcardList)) {
            return ResponseUtil.success();
        }
        List<CollectionBankcardVo> list = new ArrayList<>();
        CollectionBankcardVo vo = null;
        for (CollectionBankcard bankcard : bankcardList) {
            vo = new CollectionBankcardVo();
            BeanUtils.copyProperties(bankcard, vo);
            String bankId = bankcard.getBankId();
            BankInfo bankInfo = null;
            if (!ObjectUtils.isEmpty(bankId)) {
                bankInfo = bankInfoService.findById(Long.parseLong(bankId));
            }
            if (bankInfo != null) {
                vo.setBankName(bankInfo.getBankName());
            }
            list.add(vo);
        }
        return ResponseUtil.success(list);
    }

    @PostMapping("/submit")
    @ApiOperation("提交充值")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "chargeAmount", value = "充值金额", required = false),
            @ApiImplicitParam(name = "remitType", value = "汇款方式，银行卡1，支付宝2，微信3,该字段暂时弃用，后端固定成银行卡", required = false),
            @ApiImplicitParam(name = "remitterName", value = "汇款人", required = false),
            @ApiImplicitParam(name = "bankcardId", value = "收款银行卡ID", required = false),
    })
    public ResponseEntity submitCharge(@RequestPart(value = "file", required = false) MultipartFile file, String chargeAmount, Integer remitType, String remitterName, Long bankcardId){
        if (CasinoWebUtil.checkNull(chargeAmount,remitterName,bankcardId)) {
            return ResponseUtil.parameterNotNull();
        }
        log.info("收到提交充值请求chargeAmount:{} remitterName:{} bankcardId:{} file:{}",chargeAmount,remitterName,bankcardId,file);
        //根据产品呀要求，前端暂时注释掉汇款方式，前端发起的充值默认就是银行卡充值
        remitType = Constants.remitType_bank;
        ResponseEntity responseEntity = null;
        Long authId = CasinoWebUtil.getAuthId();
        String key = MessageFormat.format(RedisLockUtil.RECHARGE_REQUEST,authId.toString());
        Boolean lock = false;
        try {
            lock = redisLockUtil.getLock(key, authId.toString());
            if (lock) {
                log.info("充值申请取到redis锁{}",key);
                responseEntity =
                    chargeBusiness.submitOrder(file, chargeAmount, remitType, remitterName, bankcardId, authId);
            }else {
                return ResponseUtil.custom("请重试一次");
            }
        }catch (Exception ex){
            log.error("充值请求异常{}",ex.getMessage());
            return ResponseUtil.custom("请重试一次");
        }finally {
            if (lock){
                log.info("释放充值redis锁{}",key);
                redisLockUtil.releaseLock(key, authId.toString());
            }
        }
        return responseEntity;
    }
}
