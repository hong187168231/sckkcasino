package com.qianyi.casinoadmin.controller;


import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.CompanyManagement;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.CompanyManagementService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @ApiOperation("公司列表")
    @GetMapping("/findCompany")
    public ResponseEntity<CompanyManagement> findCompany(){
        return ResponseUtil.success(companyManagementService.findAll());
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
    public ResponseEntity deleteCompany(Long id, String companyName){
        CompanyManagement companyManagement = companyManagementService.findById(id);
        if(companyManagement == null){
            return ResponseUtil.custom("公司不存在");
        }
      return null;
    }
}
