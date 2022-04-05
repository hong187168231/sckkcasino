package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.RebateConfiguration;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.RebateConfigurationService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.ProxyWashCodeConfigVo;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Api(tags = "代理中心")
@RestController
@Slf4j
@RequestMapping("/rebateProxyConfiguration")
public class RebateProxyConfigurationController {
    @Autowired
    private RebateConfigurationService rebateConfigurationService;
    @Autowired
    private ProxyUserService proxyUserService;

    @ApiOperation("基层代理返利比例")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "当前详情页面基层代理id", required = true),
    })
    @GetMapping("/findAll")
    public ResponseEntity<ProxyWashCodeConfigVo> findAll(Long id){
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("代理不存在");
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("代理级别不对应");
        }
        RebateConfiguration byThirdProxy = rebateConfigurationService.findByUserIdAndType(id, Constants.PROXY_TYPE);
        ProxyWashCodeConfigVo proxyWashCodeConfigVo = new ProxyWashCodeConfigVo();
        if (!LoginUtil.checkNull(byThirdProxy)){
            BeanUtils.copyProperties(byThirdProxy, proxyWashCodeConfigVo);
        }
        return ResponseUtil.success(proxyWashCodeConfigVo);
    }

    @ApiOperation("保存")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "proxyWashCodeConfig", value = "洗码抽点比例对象", required = false),
        @ApiImplicitParam(name = "proxyUserId", value = "当前详情基层代理id", required = true),
    })
    @PostMapping("/update")
    public ResponseEntity updateProxyWashCodeConfig(RebateConfiguration proxyWashCodeConfig,Long proxyUserId){
        if (LoginUtil.checkNull(proxyUserId)){
            return ResponseUtil.custom("参数必填");
        }
        ProxyUser byId = proxyUserService.findById(proxyUserId);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("代理不存在");
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("代理级别不对应");
        }
        RebateConfiguration byThirdProxy = rebateConfigurationService.findByUserIdAndType(proxyUserId, Constants.PROXY_TYPE);
        if (LoginUtil.checkNull(proxyWashCodeConfig)){
            return ResponseUtil.custom("参数必填");
        }
        if (LoginUtil.checkNull(proxyWashCodeConfig.getCQ9Rate(),proxyWashCodeConfig.getPGRate(),proxyWashCodeConfig.getWMRate())){
            return ResponseUtil.custom("参数必填");
        }
        if (proxyWashCodeConfig.getCQ9Rate().compareTo(new BigDecimal(CommonConst.NUMBER_100)) > 0 || proxyWashCodeConfig.getCQ9Rate().compareTo(BigDecimal.ZERO) < 0){
            return ResponseUtil.custom("参数不合法");
        }
        if (proxyWashCodeConfig.getWMRate().compareTo(new BigDecimal(CommonConst.NUMBER_100)) > 0 || proxyWashCodeConfig.getWMRate().compareTo(BigDecimal.ZERO) < 0){
            return ResponseUtil.custom("参数不合法");
        }
        if (proxyWashCodeConfig.getPGRate().compareTo(new BigDecimal(CommonConst.NUMBER_100)) > 0 || proxyWashCodeConfig.getPGRate().compareTo(BigDecimal.ZERO) < 0){
            return ResponseUtil.custom("参数不合法");
        }


        if (LoginUtil.checkNull(byThirdProxy)){
            if (proxyWashCodeConfig.getPGRate().compareTo(BigDecimal.ZERO) == 0 &&
                proxyWashCodeConfig.getCQ9Rate().compareTo(BigDecimal.ZERO) == 0 &&
                proxyWashCodeConfig.getWMRate().compareTo(BigDecimal.ZERO) == 0){
                return ResponseUtil.success();
            }
            byThirdProxy = new RebateConfiguration();
            byThirdProxy.setUserId(proxyUserId);
            byThirdProxy.setType(Constants.PROXY_TYPE);
        }else {
            if (proxyWashCodeConfig.getPGRate().compareTo(BigDecimal.ZERO) == 0 &&
                proxyWashCodeConfig.getCQ9Rate().compareTo(BigDecimal.ZERO) == 0 &&
                proxyWashCodeConfig.getWMRate().compareTo(BigDecimal.ZERO) == 0){
                rebateConfigurationService.delete(proxyUserId,byThirdProxy);
                return ResponseUtil.success();
            }
        }


        byThirdProxy.setCQ9Rate(proxyWashCodeConfig.getCQ9Rate());
        byThirdProxy.setPGRate(proxyWashCodeConfig.getPGRate());
        byThirdProxy.setWMRate(proxyWashCodeConfig.getWMRate());

        rebateConfigurationService.save(byThirdProxy);
        return ResponseUtil.success();
    }
}
