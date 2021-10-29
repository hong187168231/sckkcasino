package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.CompanyProxyMonth;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.CompanyProxyMonthService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.PageUtil;
import com.qianyi.casinocore.vo.CompanyProxyMonthVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "代理中心")
@RestController
@RequestMapping("companyProxyMonth")
public class CompanyProxyMonthController {
    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;

    @Autowired
    private ProxyUserService proxyUserService;
    @ApiOperation("查询代理月结表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
            @ApiImplicitParam(name = "staticsTimes", value = "统计时段", required = true),
    })
    public ResponseEntity<CompanyProxyMonthVo> find(Integer pageSize, Integer pageCode, Integer proxyRole, Integer tag, String userName,
                                                    String staticsTimes){
        CompanyProxyMonth companyProxyMonth = new CompanyProxyMonth();
        Sort sort=Sort.by("proxyRole").ascending();
        companyProxyMonth.setProxyRole(proxyRole);
        companyProxyMonth.setStaticsTimes(staticsTimes);
        if (CasinoProxyUtil.setParameter(companyProxyMonth)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        List<CompanyProxyMonth> companyProxyMonths = new ArrayList<>();
        if (this.findCompanyProxyMonths(tag,userName,companyProxyMonth,sort,companyProxyMonths,byId)){
            return ResponseUtil.custom("参数不合法");
        }
        PageVo pageVO = new PageVo(pageCode,pageSize);
        PageResultVO<CompanyProxyMonth> pageResultVO = (PageResultVO<CompanyProxyMonth>) PageUtil.handlePageResult(companyProxyMonths, pageVO);
        List<CompanyProxyMonth> content = (List<CompanyProxyMonth>) pageResultVO.getContent();
        PageResultVO<CompanyProxyMonthVo> pageResult = new PageResultVO(pageResultVO);
        if (content.size() > CommonConst.NUMBER_0){
            List<CompanyProxyMonthVo> accountChangeVoList =new LinkedList<>();
            List<Long> proxyUserIds = content.stream().map(CompanyProxyMonth::getUserId).collect(Collectors.toList());
            List<ProxyUser> proxyUser = proxyUserService.findProxyUser(proxyUserIds);
            if(proxyUser != null){
                content.stream().forEach(proxyDetail ->{
                    CompanyProxyMonthVo vo = new CompanyProxyMonthVo(proxyDetail);
                    proxyUser.stream().forEach(proxy->{
                        if (proxy.getId().equals(proxyDetail.getUserId())){
                            vo.setUserName(proxy.getUserName());
                            vo.setNickName(proxy.getNickName());
                        }
                    });
                    accountChangeVoList.add(vo);
                });
                content.clear();
                proxyUser.clear();
            }
            pageResult.setContent(accountChangeVoList);
        }
        return ResponseUtil.success(pageResult);
    }
    private Boolean findCompanyProxyMonths(Integer tag, String userName, CompanyProxyMonth companyProxyMonth, Sort sort,
                                           List<CompanyProxyMonth> companyProxyMonthList,ProxyUser byId){
        List<CompanyProxyMonth> companyProxyMonths;
        if (!CasinoProxyUtil.checkNull(userName)){
            if (CasinoProxyUtil.checkNull(tag)){
                return true;
            }
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                return false;
            }
            if (byId.getProxyRole() == byUserName.getProxyRole() && byId.getId() != byUserName.getId()){//不能搜同级别的
                return false;
            }
            if (byId.getProxyRole() > byUserName.getProxyRole()){//不能搜上级的
                return false;
            }
            if (tag == CommonConst.NUMBER_1){//包含下级
                if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    companyProxyMonth.setFirstProxy(byUserName.getId());
                    companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
                }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    companyProxyMonth.setSecondProxy(byUserName.getId());
                    companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
                    companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() != CommonConst.NUMBER_1)).collect(Collectors.toList());
                }else {
                    companyProxyMonth.setThirdProxy(byUserName.getId());
                    companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
                    companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() == CommonConst.NUMBER_3)).collect(Collectors.toList());
                }
            }else {
                companyProxyMonth.setUserId(byUserName.getId());
                companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
            }
        }else {
            companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
            if (byId.getProxyRole() == CommonConst.NUMBER_2){
                companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() != CommonConst.NUMBER_1)).collect(Collectors.toList());
            }else if(byId.getProxyRole() == CommonConst.NUMBER_3){
                companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() == CommonConst.NUMBER_3)).collect(Collectors.toList());
            }
        }
        companyProxyMonthList.addAll(companyProxyMonths);
        return false;
    }
}
