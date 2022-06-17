package com.qianyi.casinoadmin.controller;


import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.CompanyManagement;
import com.qianyi.casinocore.service.CompanyManagementService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @ApiOperation("公司列表")
    @GetMapping("/findCompany")
    public ResponseEntity<CompanyManagement> findCompany(Long id, String companyName){
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

}
