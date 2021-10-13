package com.qianyi.casinoproxy.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.PasswordUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.ProxyUserVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.casinoproxy.util.LoginUtil;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
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
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        proxyUser.setId(authId);
        if (tag == CommonConst.NUMBER_1 && CasinoProxyUtil.checkNull(userName)){
            if (byId.getProxyRole() == CommonConst.NUMBER_1){
                proxyUser.setFirstProxy(authId);
                proxyUser.setId(null);
            }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
                proxyUser.setSecondProxy(authId);
                proxyUser.setId(null);
            }
        }else if(tag == CommonConst.NUMBER_1 && !CasinoProxyUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (byUserName.getProxyRole() == byId.getProxyRole()){//级别相同
                if (byUserName.getId() == authId){//自己查自己
                    if (byUserName.getProxyRole() == CommonConst.NUMBER_1){//自己总代查自己
                        proxyUser.setFirstProxy(authId);
                        proxyUser.setId(null);
                    }else if(byId.getProxyRole() == CommonConst.NUMBER_2){//自己区域查自己
                        proxyUser.setSecondProxy(authId);
                        proxyUser.setId(null);
                    }
                }else {
                    ResponseUtil.success(new PageResultVO());
                }
            }else if(byUserName.getProxyRole() > byId.getProxyRole()){
                if (byUserName.getProxyRole() == CommonConst.NUMBER_2 && byUserName.getFirstProxy() == authId){//1级搜2级
                    proxyUser.setSecondProxy(byUserName.getId());
                    proxyUser.setId(null);
                }else if (byUserName.getProxyRole() == CommonConst.NUMBER_3 && (byUserName.getSecondProxy() == authId||byUserName.getFirstProxy() == authId)){//2级搜3级 1级搜3级
                    proxyUser.setUserName(userName);
                    proxyUser.setId(null);
                }else {//没有权限
                    ResponseUtil.success(new PageResultVO());
                }
            }else {
                ResponseUtil.success(new PageResultVO());
            }
        }else if(tag == CommonConst.NUMBER_0 && !CasinoProxyUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if ((byUserName.getSecondProxy() == authId||byUserName.getFirstProxy() == authId) || byUserName.getId() == authId){
                proxyUser.setUserName(userName);
                proxyUser.setId(null);
            }else {
                ResponseUtil.success(new PageResultVO());
            }
        }else {
            ResponseUtil.success(new PageResultVO());
        }
        Page<ProxyUser> proxyUserPage = proxyUserService.findProxyUserPage(pageable, proxyUser, startDate, endDate);
        PageResultVO<ProxyUserVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<ProxyUser> proxyUserList = proxyUserPage.getContent();
        if(proxyUserList != null && proxyUserList.size() > 0){
            List<ProxyUserVo> userVoList = new LinkedList();
            List<Long> firstProxyIds = proxyUserList.stream().filter(item -> item.getProxyRole() == CommonConst.NUMBER_2).map(ProxyUser::getFirstProxy).collect(Collectors.toList());
            List<Long> secondProxyIds = proxyUserList.stream().filter(item -> item.getProxyRole() == CommonConst.NUMBER_3).map(ProxyUser::getSecondProxy).collect(Collectors.toList());
            firstProxyIds.addAll(secondProxyIds);
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(firstProxyIds);
            if (proxyUsers != null){
                proxyUserList.stream().forEach(u -> {
                    ProxyUserVo proxyUserVo = new ProxyUserVo(u);
                    proxyUsers.stream().forEach(proxy->{
                        if (u.getProxyRole() == CommonConst.NUMBER_2 && u.getFirstProxy().equals(proxy.getId())){
                            proxyUserVo.setSuperiorProxyAccount(proxy.getUserName());
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getSecondProxy().equals(proxy.getId())){
                            proxyUserVo.setSuperiorProxyAccount(proxy.getUserName());
                        }
                    });
                    userVoList.add(proxyUserVo);
                });
            }
            pageResultVO.setContent(userVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }
    @ApiOperation("添加代理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "账号", required = true),
            @ApiImplicitParam(name = "nickName", value = "用户昵称", required = true),
    })
    @PostMapping("saveProxyUser")
    public ResponseEntity saveProxyUser(String userName, String nickName){
        if (CasinoProxyUtil.checkNull(userName,nickName)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byUserName = proxyUserService.findByUserName(userName);
        if (!CasinoProxyUtil.checkNull(byUserName)){
            return ResponseUtil.custom("代理账号重复");
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        if (byId.getProxyRole() == CommonConst.NUMBER_3){
            return ResponseUtil.custom("基层代理不能创建代理");
        }
        Integer proxyRole = byId.getProxyRole() + CommonConst.NUMBER_1;
        ProxyUser proxyUser = new ProxyUser();
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            proxyUser.setFirstProxy(byId.getId());
        }else {
            proxyUser.setFirstProxy(byId.getFirstProxy());
            proxyUser.setSecondProxy(byId.getId());
            proxyUser.setProxyCode(LoginUtil.getProxyCode());
        }
        proxyUser.setUserName(userName);
        proxyUser.setNickName(nickName);
        //随机生成
        String password = PasswordUtil.getRandomPwd();
        String bcryptPassword = CasinoProxyUtil.bcrypt(password);
        proxyUser.setPassWord(bcryptPassword);
        proxyUser.setProxyRole(proxyRole);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        ProxyUser saveProxyUser = proxyUserService.save(proxyUser);
        if (saveProxyUser.getProxyRole() == CommonConst.NUMBER_2 && !CasinoProxyUtil.checkNull(saveProxyUser)){
            saveProxyUser.setSecondProxy(saveProxyUser.getId());
            proxyUserService.save(saveProxyUser);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", userName);
        jsonObject.put("password", password);
        return ResponseUtil.success(jsonObject);
    }
    @ApiOperation("重置密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("resetPasswords")
    public ResponseEntity resetPasswords(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        //随机生成
        String password = PasswordUtil.getRandomPwd();
        String bcryptPassword = CasinoProxyUtil.bcrypt(password);
        byId.setPassWord(bcryptPassword);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", byId.getUserName());
        jsonObject.put("password", password);
        proxyUserService.save(byId);
        return ResponseUtil.success(jsonObject);
    }
    @ApiOperation("锁定解锁")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("updateUserFlag")
    public ResponseEntity updateUserFlag(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (byId.getUserFlag() == CommonConst.NUMBER_1){
            byId.setUserFlag(CommonConst.NUMBER_2);
        }else {
            byId.setUserFlag(CommonConst.NUMBER_1);
        }
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }
    @ApiOperation("升级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("upgrade")
    public ResponseEntity upgrade(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("只有基层代理可以升级");
        }
        byId.setProxyRole(CommonConst.NUMBER_2);
        byId.setSecondProxy(byId.getId());
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }
    @ApiOperation("转移下级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "passivityId", value = "被转移的id", required = true),
            @ApiImplicitParam(name = "id", value = "接受id", required = true),
    })
    @GetMapping("transfer")
    public ResponseEntity transfer(Long passivityId,Long id){
        if (CasinoProxyUtil.checkNull(id,passivityId)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser passivity = proxyUserService.findById(passivityId);
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(passivity,byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (passivity.getProxyRole() != CommonConst.NUMBER_2 || byId.getProxyRole() != CommonConst.NUMBER_2 ){
            return ResponseUtil.custom("只有区域代理可以转移下级");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setSecondProxy(passivity.getId());
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList.stream().forEach(proxy -> {
            proxy.setSecondProxy(byId.getId());
            proxyUserService.save(proxy);
        });
        return ResponseUtil.success();
    }
    @ApiOperation("删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("delete")
    public ResponseEntity delete(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if ( byId.getProxyRole() == CommonConst.NUMBER_1 ){
            return ResponseUtil.custom("总代不能删除");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setSecondProxy(byId.getId());
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        if (proxyUserList != null && proxyUserList.size() > CommonConst.NUMBER_1){
            return ResponseUtil.custom("该代理的下级仍未转移");
        }
        byId.setIsDelete(CommonConst.NUMBER_2);
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }
}
