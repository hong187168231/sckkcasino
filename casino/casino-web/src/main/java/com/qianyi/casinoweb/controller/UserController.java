package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.UserVo;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("user")
@Api(tags = "用户中心")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    UserMoneyService userMoneyService;
    @Autowired
    BankcardsService bankcardsService;

    @GetMapping("info")
    @ApiOperation("获取当前用户的基本信息")
    public ResponseEntity<UserVo> info() {
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        UserVo vo = new UserVo();
        vo.setUserId(user.getId());
        vo.setAccount(user.getAccount());
        vo.setName(user.getName());
        vo.setHeadImg(user.getHeadImg());
        //去银行卡查询真实姓名
        Bankcards bankcards = bankcardsService.findBankCardsInByUserId(authId);
        String realName = bankcards == null ? null : bankcards.getRealName();
        vo.setRealName(realName);
        //TODO 查询可提金额，未完成流水(打码量)
        UserMoney userMoney = userMoneyService.findByUserId(user.getId());
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        if (userMoney == null) {
            vo.setUnfinshTurnover(defaultVal);
            vo.setDrawMoney(defaultVal);
            vo.setMoney(defaultVal);
            return new ResponseEntity(ResponseCode.SUCCESS, vo);
        }
        BigDecimal money = userMoney.getMoney() == null ? defaultVal : userMoney.getMoney().setScale(2, BigDecimal.ROUND_HALF_UP);
        vo.setMoney(money);
        vo.setDrawMoney(userMoney.getWithdrawMoney());
        BigDecimal codeNum = userMoney.getCodeNum() == null ? defaultVal : userMoney.getCodeNum().setScale(2, BigDecimal.ROUND_HALF_UP);
        vo.setUnfinshTurnover(codeNum);
        return new ResponseEntity(ResponseCode.SUCCESS, vo);
    }

    @PostMapping("/updateUserInfo")
    @ApiOperation("登录用户修改信息")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "参数类型:0=真实姓名，1=邮箱地址，2=微信账号，3=QQ账号", required = true),
            @ApiImplicitParam(name = "value", required = false),
    })
    public ResponseEntity updateUserInfo(String type, String value) {
        if (ObjectUtils.isEmpty(type)) {
            return ResponseUtil.custom("type字段不允许为空");
        }
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        if (user == null) {
            return ResponseUtil.custom("用户不存在");
        }
        if ("0".equals(type)) {
            user.setName(value);
        } else if ("1".equals(type)) {
            user.setEmail(value);
        } else if ("2".equals(type)) {
            user.setWebChat(value);
        } else if ("3".equals(type)) {
            user.setQq(value);
        } else{
            return ResponseUtil.custom("type字段值仅限于0,1,2,3");
        }
        userService.save(user);
        return ResponseUtil.success();
    }


    @PostMapping("/webUpdateUserInfo")
    @ApiOperation("web端登录用户修改信息")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "邮箱"),
            @ApiImplicitParam(name = "webChat", value = "微信"),
            @ApiImplicitParam(name = "qq", value = "QQ"),
            @ApiImplicitParam(name = "phone", value = "手机号"),
    })
    public ResponseEntity webUpdateUserInfo(String email, String webChat, String qq, String phone) {
        String regex = "^[0-9a-zA-Z]{0,20}$";
        if (!ObjectUtils.isEmpty(webChat) && !webChat.matches(regex)) {
            return ResponseUtil.custom("微信号只能是数字或字母且长度不超过20");
        }
        if (!ObjectUtils.isEmpty(qq) && !qq.matches(regex)) {
            return ResponseUtil.custom("QQ号只能是数字或字母且长度不超过20");
        }
        if (!ObjectUtils.isEmpty(phone) && !phone.matches(regex)) {
            return ResponseUtil.custom("手机号只能是数字或字母且长度不超过20");
        }
        String emailRegex = "\\w+@\\w+(\\.\\w{2,3})*\\.\\w{2,3}";
        if (!ObjectUtils.isEmpty(email) && !email.matches(emailRegex)) {
            return ResponseUtil.custom("邮箱格式填写错误");
        }
        //这4个字段数据添加后不能再修改
        boolean flag = true;
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        if (ObjectUtils.isEmpty(user.getEmail())) {
            user.setEmail(email);
            flag = false;
        }
        if (ObjectUtils.isEmpty(user.getWebChat())) {
            user.setWebChat(webChat);
            flag = false;
        }
        if (ObjectUtils.isEmpty(user.getQq())) {
            user.setQq(qq);
            flag = false;
        }
        if (ObjectUtils.isEmpty(user.getPhone())) {
            user.setPhone(phone);
            flag = false;
        }
        if(flag){
            return ResponseUtil.custom("信息已录入，不允许修改");
        }
        userService.save(user);
        return ResponseUtil.success();
    }
}
