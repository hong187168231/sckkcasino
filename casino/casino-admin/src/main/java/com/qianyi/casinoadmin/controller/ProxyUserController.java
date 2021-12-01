package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyCommission;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyCommissionService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.PasswordUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.ProxyUserVo;
import com.qianyi.modulecommon.RegexEnum;
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

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "代理中心")
@RestController
@RequestMapping("proxyUser")
public class ProxyUserController {
    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyCommissionService proxyCommissionService;

    @Autowired
    private UserService userService;
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
        sort = sort.and(Sort.by("id").descending());
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        proxyUser.setProxyRole(proxyRole);
        proxyUser.setUserFlag(userFlag);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        if (!LoginUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            if (tag == CommonConst.NUMBER_1){
                if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    proxyUser.setFirstProxy(byUserName.getId());
                }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    proxyUser.setSecondProxy(byUserName.getId());
                }else {
                    proxyUser.setId(byUserName.getId());
                }
            }else {
                proxyUser.setUserName(userName);
            }
        }
        Page<ProxyUser> proxyUserPage = proxyUserService.findProxyUserPage(pageable, proxyUser, startDate, endDate);
        PageResultVO<ProxyUserVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<ProxyUser> proxyUserList = proxyUserPage.getContent();
        List<Long> proxyIds = proxyUserList.stream().map(ProxyUser::getId).collect(Collectors.toList());
        List<ProxyCommission> proxyCommissions = proxyCommissionService.findProxyUser(proxyIds);
        if(proxyUserList != null && proxyUserList.size() > 0){
            List<ProxyUserVo> userVoList = new LinkedList();
            List<Long> firstProxyIds = proxyUserList.stream().filter(item -> item.getProxyRole() != CommonConst.NUMBER_1).map(ProxyUser::getFirstProxy).collect(Collectors.toList());
            List<Long> secondProxyIds = proxyUserList.stream().filter(item -> item.getProxyRole() == CommonConst.NUMBER_3).map(ProxyUser::getSecondProxy).collect(Collectors.toList());
            firstProxyIds.addAll(secondProxyIds);
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(firstProxyIds);
            if (proxyUsers != null){
                proxyUserList.stream().forEach(u -> {
                    ProxyUserVo proxyUserVo = new ProxyUserVo(u);
                    proxyUsers.stream().forEach(proxy->{
                        if (u.getProxyRole() == CommonConst.NUMBER_2 && u.getFirstProxy().equals(proxy.getId())){
                            proxyUserVo.setSuperiorProxyAccount(proxy.getUserName());
                            proxyUserVo.setFirstProxyAccount(proxy.getUserName());
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getSecondProxy().equals(proxy.getId())){
                            proxyUserVo.setSuperiorProxyAccount(proxy.getUserName());
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getFirstProxy().equals(proxy.getId())){
                            proxyUserVo.setFirstProxyAccount(proxy.getUserName());
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_1){
                            proxyUserVo.setFirstProxyAccount(proxyUserVo.getUserName());
                        }
                    });
                    proxyCommissions.stream().forEach(proxyCommission->{
                        if (u.getProxyRole() == CommonConst.NUMBER_3 && u.getId().equals(proxyCommission.getProxyUserId())){
                            String commissionRatio = MessageFormat.format(CommonConst.THIRD_FORMAT,proxyCommission.getFirstCommission() == null?"0":proxyCommission.getFirstCommission().multiply(CommonConst.BIGDECIMAL_100),
                                    proxyCommission.getSecondCommission() == null?"0":proxyCommission.getSecondCommission().multiply(CommonConst.BIGDECIMAL_100),
                                    proxyCommission.getThirdCommission() == null?"0":proxyCommission.getThirdCommission().multiply(CommonConst.BIGDECIMAL_100));
                            proxyUserVo.setCommissionRatio(commissionRatio);
                        }
                        if (u.getProxyRole() == CommonConst.NUMBER_2 && u.getId().equals(proxyCommission.getProxyUserId())){
                            String commissionRatio = MessageFormat.format(CommonConst.SECOND_FORMAT,proxyCommission.getFirstCommission() == null?"0":proxyCommission.getFirstCommission().multiply(CommonConst.BIGDECIMAL_100));
                            proxyUserVo.setCommissionRatio(commissionRatio);
                        }
                    });
                    if (u.getProxyRole() == CommonConst.NUMBER_3){
                        User user = new User();
                        user.setThirdProxy(u.getId());
                        Long  userCount  = userService.findUserCount(user,null,null);
                        proxyUserVo.setUsersNum(Math.toIntExact(userCount));
                    }
                    userVoList.add(proxyUserVo);
                });
            }

            pageResultVO.setContent(userVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("查询详情页面直属玩家数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("findUsersNum")
    public ResponseEntity findUsersNum(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        User user = new User();
        Long  userCount;
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            user.setFirstProxy(id);
            userCount  = userService.findUserCount(user,null,null);

        }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
            user.setSecondProxy(id);
            userCount  = userService.findUserCount(user,null,null);
        }else {
            user.setThirdProxy(id);
            userCount  = userService.findUserCount(user,null,null);
        }
        return ResponseUtil.success(userCount);
    }

    @ApiOperation("添加代理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "账号", required = true),
            @ApiImplicitParam(name = "nickName", value = "用户昵称", required = false),
    })
    @PostMapping("saveProxyUser")
    public ResponseEntity saveProxyUser(String userName, String nickName){
        if (LoginUtil.checkNull(userName)){
            return ResponseUtil.custom("参数不合法");
        }
        if (!userName.matches(RegexEnum.ACCOUNT.getRegex())){
            return ResponseUtil.custom("账号请输入6~15位数字或字母！");
        }
        if (!LoginUtil.checkNull(nickName) && !nickName.matches(RegexEnum.NAME.getRegex())){
            return ResponseUtil.custom("昵称请输入1~20位中文或字母！");
        }
        ProxyUser byUserName = proxyUserService.findByUserName(userName);
        if (!LoginUtil.checkNull(byUserName)){
            return ResponseUtil.custom("代理账号重复");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setUserName(userName);
        proxyUser.setNickName(nickName);
        //随机生成
        String password = PasswordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
        proxyUser.setPassWord(bcryptPassword);
        proxyUser.setProxyRole(CommonConst.NUMBER_1);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setProxyUsersNum(CommonConst.NUMBER_0);
        ProxyUser saveProxyUser = proxyUserService.save(proxyUser);
        if (!LoginUtil.checkNull(saveProxyUser)){
            saveProxyUser.setFirstProxy(saveProxyUser.getId());
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
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        //随机生成
        String password = PasswordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
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
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
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
    @Transactional
    public ResponseEntity upgrade(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_2){
            return ResponseUtil.custom("只有区域代理可以升级");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setSecondProxy(byId.getId());
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList.stream().forEach(proxy -> {
            proxy.setFirstProxy(byId.getId());
            proxyUserService.save(proxy);
        });
        byId.setFirstProxy(byId.getId());
        byId.setProxyRole(CommonConst.NUMBER_1);
        byId.setSecondProxy(null);
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
        if (LoginUtil.checkNull(id,passivityId)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser passivity = proxyUserService.findById(passivityId);
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(passivity,byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if (passivity.getProxyRole() != CommonConst.NUMBER_1 || byId.getProxyRole() != CommonConst.NUMBER_1 ){
            return ResponseUtil.custom("只有总代可以转移下级");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setFirstProxy(passivity.getId());
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList.stream().filter(PUser -> PUser.getId() != passivityId).forEach(proxy -> {
            proxy.setFirstProxy(byId.getId());
            proxyUserService.save(proxy);
        });
        return ResponseUtil.success();
    }
    @ApiOperation("获取下拉框数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
    })
    @GetMapping("findProxyUserList")
    public ResponseEntity<ProxyUser> findProxyUserList(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setProxyRole(CommonConst.NUMBER_1);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        proxyUserList = proxyUserList == null?new ArrayList<>(): proxyUserList.stream().filter(PUser -> PUser.getId() != id).collect(Collectors.toList());
        return ResponseUtil.success(proxyUserList);
    }
    @ApiOperation("删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
    })
    @GetMapping("delete")
    public ResponseEntity delete(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        if ( byId.getProxyRole() != CommonConst.NUMBER_1 ){
            return ResponseUtil.custom("只有总代可以删除");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setFirstProxy(byId.getId());
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        if (proxyUserList != null && proxyUserList.size() > CommonConst.NUMBER_1){
            return ResponseUtil.custom("该代理的下级仍未转移");
        }
        byId.setIsDelete(CommonConst.NUMBER_2);
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }
}
