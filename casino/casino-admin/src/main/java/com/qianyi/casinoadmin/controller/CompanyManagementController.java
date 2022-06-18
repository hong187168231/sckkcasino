package com.qianyi.casinoadmin.controller;


import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.service.CompanyProxyMonthService;
import com.qianyi.casinocore.vo.CompanyVo;
import com.qianyi.casinocore.model.CompanyManagement;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.CompanyManagementService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 代理中心公司管理
 */
@Api(tags = "代理中心")
@RestController
@RequestMapping("company")
public class CompanyManagementController {

    @Autowired
    private CompanyManagementService companyManagementService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;



    @ApiOperation("公司列表")
    @GetMapping("/findCompany")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyName", value = "公司名称", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间", required = true),
    })
    public ResponseEntity<CompanyVo> findProxyCompanyDetail(String companyName, String startDate, String endDate){
        //查询出来各公司下面总代数量
        List<Map> groupByCount = companyManagementService.findGroupByCount(companyName);

        List<CompanyVo> companyVos = getCompanyVos(groupByCount);
        //查询总佣金
        if(companyVos.isEmpty()){
            return ResponseUtil.success();
        }

        //得到总代Id集合
        Set<Long> idList = companyVos.stream().map(CompanyVo::getId).collect(Collectors.toSet());
        List<Long> proxyIdList = proxyUserService.findByCompanyIdList(idList);


        List<Map> companySumMap = companyProxyMonthService.sumCompanyProxyMonth(proxyIdList, startDate, endDate);

        List<CompanyVo> companySum = getCompanyVosMap(companySumMap);
        companyVos.forEach(companyVo -> {
            companySum.forEach(companyVo1 -> {
                if(companyVo1.getId() == companyVo.getId()){
                    companyVo.setProxyCommission(companyVo1.getProxyCommission());
                    companyVo.setProxyOextract(companyVo1.getProxyOextract());
                    companyVo.setCreateDate(companyVo1.getCreateDate());
                    companyVo.setCreateName(companyVo1.getCreateName());
                }
            });
        });
        return ResponseUtil.success(companyVos);
    }

    private List<CompanyVo> getCompanyVosMap(List<Map> companySumMap) {
        List<CompanyVo> companyVos = new ArrayList<>();
        for (Map map : companySumMap) {
            CompanyVo companyVo = CompanyVo.builder().id(Long.parseLong(map.get("id").toString()))
                .proxyOextract(BigDecimal.valueOf(Double.parseDouble(map.get("proxyOextract") + "")))
                .proxyCommission(BigDecimal.valueOf(Double.parseDouble(map.get("proxyCommission") + ""))).build();
            companyVos.add(companyVo);
        }
        return companyVos;
    }

    private List<CompanyVo> getCompanyVos(List<Map> groupByCount) {
        List<CompanyVo> companyVos = new ArrayList<>();
        for (Map map : groupByCount) {
            CompanyVo companyVo = CompanyVo.builder()
            .id(Long.parseLong(map.get("id").toString()))
            .proxyNum(Integer.parseInt(map.get("proxyNum").toString()))
            .companyName(map.get("companyName") + "")
            .createDate(map.get("createDate") + "")
            .createName(map.get("createName") + "").build();
            companyVos.add(companyVo);
        }
        return companyVos;
    }


    @ApiOperation("新增公司")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "公司Id" , required = false),
            @ApiImplicitParam(name = "companyName", value = "公司名称" , required = true),
    })
    @PostMapping("/saveCompany")
    public ResponseEntity saveCompany(Long id, String companyName){
        if (LoginUtil.checkNull(companyName)){
            return ResponseUtil.custom("参数不合法");
        }

        CompanyManagement companyManagement;
        if(id != null){
            companyManagement = companyManagementService.findById(id);
        }else{
            companyManagement = new CompanyManagement();
        }
        companyManagement.setCompanyName(companyName);
        companyManagementService.saveOrUpdate(companyManagement);
        return ResponseUtil.success(companyManagement);
    }

    @ApiOperation("设置总代所属公司")
    @PostMapping("/setPeoxyCompany")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "公司Id" , required = true),
            @ApiImplicitParam(name = "proxyId", value = "代理Id" , required = true),
    })
    public ResponseEntity setPeoxyCompany(Long id, @RequestBody List<Long> proxyList){
        CompanyManagement companyManagement = companyManagementService.findById(id);
        if(companyManagement == null){
            return ResponseUtil.custom("公司不存在");
        }
        List<ProxyUser> proxyUser = proxyUserService.findFistProUser(proxyList);
        if(proxyUser.isEmpty()){
            return ResponseUtil.custom("代理不存在");
        }
        proxyUser.stream().forEach(P -> P.setCompanyId(id));

        proxyUserService.saveList(proxyUser);
        return ResponseUtil.success();
    }

    @ApiOperation("删除公司")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "公司Id" , required = true)
    })
    @PostMapping("/deleteCompany")
    public ResponseEntity deleteCompany(Long id){
        CompanyManagement companyManagement = companyManagementService.findById(id);
        if(companyManagement == null){
            return ResponseUtil.custom("公司不存在");
        }
        //查询次公司下是否有代理
        int count = proxyUserService.findByCompanyId(id);
        if(count > 0){
            return ResponseUtil.custom("请先转移代理");
        }
        companyManagementService.deleteId(id);
        return ResponseUtil.success();
    }
}
