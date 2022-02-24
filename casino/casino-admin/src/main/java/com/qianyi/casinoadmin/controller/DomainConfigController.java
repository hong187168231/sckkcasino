package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.DomainConfig;
import com.qianyi.casinocore.service.DomainConfigService;
import com.qianyi.modulecommon.annotation.NoAuthorization;
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

import java.math.BigDecimal;


@RestController
@RequestMapping("/domain")
@Api(tags = "域名管理")
public class DomainConfigController {

    @Autowired
    private DomainConfigService domainConfigService;

    @GetMapping("/findList")
    @ApiOperation("银行列表")
    @NoAuthorization
    public ResponseEntity<DomainConfig> findList() {
        return ResponseUtil.success(domainConfigService.findList());
    }

    @ApiOperation("新增或者修改域名")
    @PostMapping(value = "/saveDomain",name = "新增或者修改域名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "domainName", value = "域名名称", required = false),
            @ApiImplicitParam(name = "domainUrl", value = "域名地址", required = true),
            @ApiImplicitParam(name = "id", value = "id", required = false),
            @ApiImplicitParam(name = "domainStatus", value = "状态 1：禁用， 1：启用", required = true)})
    public ResponseEntity<DomainConfig> saveDomain(String domainName,String domainUrl,Long id, Integer domainStatus){
        if (LoginUtil.checkNull(domainUrl) || domainStatus == null){
            ResponseUtil.custom("参数不合法");
        }
        DomainConfig domainConfig = new DomainConfig();
        if(id != null){
            DomainConfig domain = domainConfigService.findById(id);
            if(domain != null){
                domainConfig = domain;
            }
        }

        domainConfig.setDomainName(domainName);
        domainConfig.setDomainUrl(domainUrl);
        domainConfig.setDomainStatus(domainStatus);
        domainConfigService.save(domainConfig);
        return ResponseUtil.success(domainConfig);
    }
}
