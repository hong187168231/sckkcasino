package com.qianyi.casinoadmin.controller;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.CompanyManagement;
import com.qianyi.casinocore.service.CompanyManagementService;
import com.qianyi.casinocore.util.DataConst;
import com.qianyi.casinocore.vo.CompanyManagementVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.MessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "公共资源")
@RestController
@Slf4j
@RequestMapping("common")
public class CommonController {

    @Autowired
    private CompanyManagementService companyManagementService;

    @Autowired
    private MessageUtil messageUtil;

    @ApiOperation("查询公司列表下拉框")
    @GetMapping("/findCompanyList")
    @NoAuthorization
    public ResponseEntity<CompanyManagementVo> findCompanyList() {
        List<CompanyManagement> all = companyManagementService.findAll();
        List<CompanyManagementVo> companyManagementVos = new ArrayList<>();
        if (CollUtil.isNotEmpty(all)) {
            all.forEach(companyManagement -> {
                CompanyManagementVo vo = new CompanyManagementVo();
                BeanUtils.copyProperties(companyManagement, vo);
                companyManagementVos.add(vo);
            });
        }
        return ResponseUtil.success(companyManagementVos);
    }

    @ApiOperation("查询平台列表")
    @GetMapping("/getData")
    @NoAuthorization
    public ResponseEntity getData() {
        return ResponseUtil.success(DataConst.platforms);
    }

    @ApiOperation("查询平台列表(报表)")
    @GetMapping("/getReportData")
    @NoAuthorization
    public ResponseEntity getReportData() {
        return ResponseUtil.success(DataConst.platformsReport);
    }

    @ApiOperation("查询账变类型")
    @GetMapping("/findAccountChangeEnum")
    @NoAuthorization
    public ResponseEntity findAccountChangeEnum() {
        AccountChangeEnum[] values = AccountChangeEnum.values();
        Map<Integer, String> map = new HashMap<>();
        for (AccountChangeEnum accountChangeEnum : values) {
            map.put(accountChangeEnum.getType(), messageUtil.get(accountChangeEnum.getName()));
        }
        return ResponseUtil.success(map);
    }
}
