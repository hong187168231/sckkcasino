package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.CompanyProxyDetailService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.PageUtil;
import com.qianyi.casinocore.vo.CompanyProxyDetailVo;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "代理中心")
@RestController
@RequestMapping("companyProxyDetail")
public class CompanyProxyDetailController {
    @Autowired
    private CompanyProxyDetailService companyProxyDetailService;

    @Autowired
    private ProxyUserService proxyUserService;
    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyDetailVo> find(Integer pageSize, Integer pageCode, Integer proxyRole, Integer tag, String userName,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        CompanyProxyDetail companyProxyDetail = new CompanyProxyDetail();
        Sort sort=Sort.by("proxyRole").ascending();
        companyProxyDetail.setProxyRole(proxyRole);
        if (CasinoProxyUtil.setParameter(companyProxyDetail)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        List<CompanyProxyDetail> companyProxyDetails = new ArrayList<>();
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        if (this.findCompanyProxyDetailVos(tag,userName,companyProxyDetail,sort,startDate,endDate,companyProxyDetails,byId)){
            return ResponseUtil.custom("参数不合法");
        }
        List<CompanyProxyDetailVo> companyProxyDetailVos = new LinkedList<>();
        this.allCompanyProxyDetail(companyProxyDetails,companyProxyDetailVos);
        PageVo pageVO = new PageVo(pageCode,pageSize);
        PageResultVO<CompanyProxyDetailVo> pageResultVO = (PageResultVO<CompanyProxyDetailVo>) PageUtil.handlePageResult(companyProxyDetailVos, pageVO);
        List<CompanyProxyDetailVo> content = (List<CompanyProxyDetailVo>) pageResultVO.getContent();
        if (content.size() > CommonConst.NUMBER_0){
            List<Long> proxyUserIds = content.stream().map(CompanyProxyDetailVo::getProxyUserId).collect(Collectors.toList());
            List<ProxyUser> proxyUser = proxyUserService.findProxyUser(proxyUserIds);
            if(proxyUser != null){
                content.stream().forEach(proxyDetail ->{
                    proxyUser.stream().forEach(proxy->{
                        if (proxy.getId().equals(proxyDetail.getProxyUserId())){
                            proxyDetail.setUserName(proxy.getUserName());
                            proxyDetail.setNickName(proxy.getNickName());
                        }
                    });
                });
            }
        }
        return ResponseUtil.success(pageResultVO);
    }
    @ApiOperation("每日结算细节")
    @GetMapping("/findDailyDetails")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前id", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyDetail> findDailyDetails(Long id,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        Sort sort=Sort.by("id").descending();
        CompanyProxyDetail companyProxyDetail = new CompanyProxyDetail();
        List<CompanyProxyDetail> companyProxyDetailList = new LinkedList<>();
        companyProxyDetail.setUserId(id);
        if (CasinoProxyUtil.setParameter(companyProxyDetail)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate,sort);
        this.assemble(companyProxyDetails,companyProxyDetailList);

        return ResponseUtil.success(companyProxyDetailList);
    }
    @ApiOperation("统计代理报表")
    @GetMapping("/findSum")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyDetail> findSum(Integer proxyRole, Integer tag, String userName,
                                                      @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                      @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        CompanyProxyDetail companyProxyDetail = new CompanyProxyDetail();
        companyProxyDetail.setProxyRole(proxyRole);
        if (CasinoProxyUtil.setParameter(companyProxyDetail)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        List<CompanyProxyDetail> companyProxyDetails = new ArrayList<>();
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        if (this.findCompanyProxyDetailVos(tag,userName,companyProxyDetail,null,startDate,endDate,companyProxyDetails,byId)){
            return ResponseUtil.custom("参数不合法");
        }
        CompanyProxyDetail companyProxyDetail1 = new CompanyProxyDetail();
        this.assemble(companyProxyDetails,companyProxyDetail1,null);
        return ResponseUtil.success(companyProxyDetail1);
    }
    @ApiOperation("统计每日结算细节")
    @GetMapping("/findDailyDetailsSum")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前代理id", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyDetail> findDailyDetailsSum(Long id,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                  @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        CompanyProxyDetail companyProxyDetail = new CompanyProxyDetail();
        companyProxyDetail.setUserId(id);
        if (CasinoProxyUtil.setParameter(companyProxyDetail)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        List<CompanyProxyDetail> companyProxyDetails = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate,null);
        CompanyProxyDetail companyProxyDetail1 = new CompanyProxyDetail();
        this.assemble(companyProxyDetails,companyProxyDetail1,null);
        return ResponseUtil.success(companyProxyDetail1);
    }
    private void allCompanyProxyDetail(List<CompanyProxyDetail> companyProxyDetails,List<CompanyProxyDetailVo> companyProxyDetailVos){
        Map<Integer, List<CompanyProxyDetail>> firstMap = companyProxyDetails.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getProxyRole));
        firstMap.forEach(((firstKey, firstValue) -> {
            List<CompanyProxyDetail> firstList = firstValue;
            Map<Long, List<CompanyProxyDetail>> secondMap = firstList.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getUserId));
            secondMap.forEach((secondKey, secondValue) -> {
                List<CompanyProxyDetail> secondList = secondValue;
                this.assemble(secondList,companyProxyDetailVos,secondKey,firstKey);
            });
        }));
    }
    private void assemble(List<CompanyProxyDetail> list,List<CompanyProxyDetailVo> companyProxyDetailVos,Long proxyId,Integer proxyRole){
        if (CasinoProxyUtil.checkNull(list) && list.size() == CommonConst.NUMBER_0)
            return;
        CompanyProxyDetailVo vo = new CompanyProxyDetailVo();
        BigDecimal groupBetAmount = list.stream().map(CompanyProxyDetail::getGroupBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal groupTotalprofit = list.stream().map(CompanyProxyDetail::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profitAmount = list.stream().map(CompanyProxyDetail::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setGroupBetAmount(groupBetAmount);
        vo.setGroupTotalprofit(groupTotalprofit);
        vo.setProfitAmount(profitAmount);
        vo.setProxyUserId(proxyId);
        vo.setProxyRole(proxyRole);
        companyProxyDetailVos.add(vo);
    }
    private Boolean findCompanyProxyDetailVos(Integer tag, String userName,CompanyProxyDetail companyProxyDetail,Sort sort,Date startDate,
                                              Date endDate,List<CompanyProxyDetail> companyProxyDetails,ProxyUser byId){
        List<CompanyProxyDetail> companyProxyDetailList = null;
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
                    companyProxyDetail.setFirstProxy(byUserName.getId());
                    companyProxyDetailList = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate,sort);
                }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    companyProxyDetail.setSecondProxy(byUserName.getId());
                    companyProxyDetailList = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate,sort);
                    companyProxyDetailList = companyProxyDetailList.stream().filter(item -> (item.getProxyRole() != CommonConst.NUMBER_1)).collect(Collectors.toList());
                }else {
                    companyProxyDetail.setThirdProxy(byUserName.getId());
                    companyProxyDetailList = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate,sort);
                    companyProxyDetailList = companyProxyDetailList.stream().filter(item -> (item.getProxyRole() == CommonConst.NUMBER_3)).collect(Collectors.toList());
                }
            }else {
                companyProxyDetail.setUserId(byUserName.getId());
                companyProxyDetailList = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate,sort);
            }
        }else {
            companyProxyDetailList = companyProxyDetailService.findCompanyProxyDetails(companyProxyDetail, startDate, endDate,sort);
            if (byId.getProxyRole() == CommonConst.NUMBER_2){
                companyProxyDetailList = companyProxyDetailList.stream().filter(item -> (item.getProxyRole() != CommonConst.NUMBER_1)).collect(Collectors.toList());
            }else if(byId.getProxyRole() == CommonConst.NUMBER_3){
                companyProxyDetailList = companyProxyDetailList.stream().filter(item -> (item.getProxyRole() == CommonConst.NUMBER_3)).collect(Collectors.toList());
            }
        }
        companyProxyDetails.addAll(companyProxyDetailList);
        return false;
    }
    private void assemble(List<CompanyProxyDetail> list,List<CompanyProxyDetail> companyProxyDetailList){
        Map<String, List<CompanyProxyDetail>> collect = list.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getStaticsTimes));
        collect.forEach((firstKey, firstValue) -> {
            List<CompanyProxyDetail> firstList = firstValue;
            CompanyProxyDetail vo = new CompanyProxyDetail();
            this.assemble(firstList,vo,firstKey);
            companyProxyDetailList.add(vo);
        });
    }
    private void assemble(List<CompanyProxyDetail> list,CompanyProxyDetail vo,String staticsTimes){
        if (CasinoProxyUtil.checkNull(list) && list.size() == CommonConst.NUMBER_0)
            return;
        BigDecimal groupBetAmount = list.stream().map(CompanyProxyDetail::getGroupBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal groupTotalprofit = list.stream().map(CompanyProxyDetail::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profitAmount = list.stream().map(CompanyProxyDetail::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer playerNum = list.stream().mapToInt(CompanyProxyDetail::getPlayerNum).sum();
        vo.setStaticsTimes(staticsTimes);
        CompanyProxyDetail companyProxyDetail = list.get(CommonConst.NUMBER_0);
        vo.setBenefitRate(companyProxyDetail.getBenefitRate());
        vo.setProfitRate(companyProxyDetail.getProfitRate());
        vo.setProfitLevel(companyProxyDetail.getProfitLevel());
        vo.setPlayerNum(playerNum);
        vo.setGroupBetAmount(groupBetAmount);
        vo.setGroupTotalprofit(groupTotalprofit);
        vo.setProfitAmount(profitAmount);
    }
}
