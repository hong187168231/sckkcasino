package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.ProxyUserVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Api(tags = "代理管理")
@RestController
@RequestMapping("proxyUser")
public class ProxyUserController {
    @Autowired
    private ProxyUserService proxyUserService;
    /**
     * 分页查询代理
     *
     * @param proxyRole 订单号
     * @param userFlag 账变类型
     * @param tag 1：含下级 0：不包含
     * @return
     */
    @ApiOperation("分页查询代理")
    @GetMapping("/findProxyUser")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userFlag", value = "1：正常 2：锁定, 3：删除", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = true),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = false),
    })
    public ResponseEntity<ProxyUserVo> findProxyUser(Integer pageSize, Integer pageCode,Integer proxyRole,Integer userFlag,Integer tag,String userName,
                                        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
                                        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        ProxyUser proxyUser = new ProxyUser();
        Sort sort=Sort.by("proxyRole").ascending();
        sort.and(Sort.by("id").descending());
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        proxyUser.setProxyRole(proxyRole);
        proxyUser.setUserFlag(userFlag);
        Long authId = CasinoProxyUtil.getAuthId();
        proxyUser.setId(authId);
        if (tag == CommonConst.NUMBER_1){
            ProxyUser byId = proxyUserService.findById(authId);
            if (byId.getProxyRole() == CommonConst.NUMBER_1){
                proxyUser.setFirstProxy(authId);
                proxyUser.setId(null);
            }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
                proxyUser.setSecondProxy(authId);
                proxyUser.setId(null);
            }
        }
        if (!CasinoProxyUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                ResponseUtil.success(new PageResultVO());
            }
            if (tag == CommonConst.NUMBER_1){
                if (byUserName.getProxyRole()> authId){
                    if (byUserName.getId() == authId){//自己搜自己
                        proxyUser.setFirstProxy(byUserName.getId());
                        proxyUser.setId(null);
                    }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){//1级搜2级
                        proxyUser.setSecondProxy(byUserName.getId());
                        proxyUser.setId(null);
                    }
                }else {
                    ResponseUtil.success(new PageResultVO());
                }
            }else {
                if (byUserName.getProxyRole()> authId){
                    proxyUser.setNickName(userName);
                }else {
                    ResponseUtil.success(new PageResultVO());
                }
            }
        }
        Page<ProxyUser> proxyUserPage = proxyUserService.findUserPage(pageable, proxyUser, startDate, endDate);
        PageResultVO<ProxyUserVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<ProxyUser> proxyUserList = proxyUserPage.getContent();
        if(proxyUserList != null && proxyUserList.size() > 0){
            List<ProxyUserVo> userVoList = new LinkedList();
            proxyUserList.stream().forEach(u -> {
                ProxyUserVo proxyUserVo = new ProxyUserVo(u);
                userVoList.add(proxyUserVo);
            });
            pageResultVO.setContent(userVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }
}
