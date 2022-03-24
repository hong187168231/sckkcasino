package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.VisitsSum;
import com.qianyi.casinocore.model.DepositSendActivity;
import com.qianyi.casinocore.model.DomainConfig;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.vo.VisitsVo;
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

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/domain")
@Api(tags = "域名管理")
public class DomainConfigController {

    @Autowired
    private DomainConfigService domainConfigService;

    @Autowired
    private VisitsService visitsService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @GetMapping("/visitsFindList")
    @ApiOperation("域名访问量统计")
    @NoAuthorization
    @ApiImplicitParams({
        @ApiImplicitParam(name = "domainName", value = "域名名称", required = false),
        @ApiImplicitParam(name = "StartTime", value = "开始时间", required = true),
        @ApiImplicitParam(name = "endTime", value = "结束时间", required = true),
    })
    public ResponseEntity<VisitsVo> visitsFindList(String domainName, String ip, String StartTime, String endTime) {
        List<Map<String, Object>> list = visitsService.findListSum(domainName, ip, StartTime, endTime);
        List<VisitsVo> visitsVoList = new ArrayList<>();
        List<VisitsSum> visitsSumList = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : list) {
            String dataStr = JSON.toJSONString(stringObjectMap);
            VisitsSum visitsSum = JSON.parseObject(dataStr, VisitsSum.class);
            visitsSumList.add(visitsSum);
        }

        if(visitsSumList== null || visitsSumList.isEmpty()){
            return ResponseUtil.success();
        }
        Map<String, List<VisitsSum>> collect = visitsSumList.stream().collect(Collectors.groupingBy(VisitsSum::getDomainName));
        for (String s : collect.keySet()) {
            VisitsVo visitsVo = new VisitsVo();
            visitsVo.setDomainName(s);
            List<VisitsSum> visitss = collect.get(s);
            visitsVo.setDomainIpCount(visitss.size());
            Integer sumDomainCount = visitss.stream().collect(Collectors.summingInt(VisitsSum::getDomainCount));
            visitsVo.setDomainCount(sumDomainCount);
            Set<Long> users = userService.findUserByRegisterDomainName(visitsVo.getDomainName(), StartTime, endTime);
            visitsVo.setNums(users.size());
            Integer chargeNums = chargeOrderService.getChargeNums(users);
            visitsVo.setChargeNums(chargeNums);
            visitsVoList.add(visitsVo);
        }
        return ResponseUtil.success(visitsVoList);
    }

    @GetMapping("/findList")
    @ApiOperation("域名列表")
    public ResponseEntity<DomainConfig> findList() {
        List<DomainConfig> domainConfigList = domainConfigService.findList();
        if(domainConfigList != null && !domainConfigList.isEmpty()){
            List<String> createBy = domainConfigList.stream().map(DomainConfig::getUpdateBy).collect(Collectors.toList());
            List<SysUser> sysUsers = sysUserService.findAll(createBy);
            domainConfigList.stream().forEach(domainConfig -> {
                sysUsers.stream().forEach(sysUser -> {
                    if (sysUser.getId().toString().equals(domainConfig.getUpdateBy() == null ? "" : domainConfig.getCreateBy())) {
                        domainConfig.setUpdateBy(sysUser.getUserName());
                    }
                });
            });
        }

        return ResponseUtil.success(domainConfigList);
    }

    @ApiOperation("新增或者修改域名")
    @PostMapping(value = "/saveDomain",name = "新增或者修改域名")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "domainName", value = "域名名称", required = false),
        @ApiImplicitParam(name = "domainUrl", value = "域名地址", required = true),
        @ApiImplicitParam(name = "id", value = "id", required = false),
        @ApiImplicitParam(name = "domainStatus", value = "状态 0：禁用， 1：启用", required = true)})
    public ResponseEntity<DomainConfig> saveDomain(String domainName,String domainUrl,Long id, Integer domainStatus){
        if (LoginUtil.checkNull(domainUrl) || domainStatus == null){
            ResponseUtil.custom("参数不合法");
        }
        if(domainStatus != CommonConst.NUMBER_0 && domainStatus != CommonConst.NUMBER_1){
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

    @ApiOperation("修改域名状态")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "id", required = true),
        @ApiImplicitParam(name = "domainStatus", value = "状态 0：禁用， 1：启用", required = true)
    })
    @PostMapping("domainStatus")
    public ResponseEntity<DomainConfig> updateDomainStatus(Long id, Integer domainStatus){
        if(domainStatus != CommonConst.NUMBER_0 && domainStatus != CommonConst.NUMBER_1){
            ResponseUtil.custom("参数不合法");
        }
        DomainConfig domainConfig = domainConfigService.findById(id);
        domainConfig.setDomainStatus(domainStatus);
        domainConfigService.save(domainConfig);
        return ResponseUtil.success(domainConfig);
    }

    @ApiOperation("删除域名")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "id", required = true)
    })
    @GetMapping("deleteId")
    public ResponseEntity<DomainConfig> deleteId(Long id){

        domainConfigService.deleteId(id);
        return ResponseUtil.success();
    }
}
