package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.UserVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.*;
import org.springframework.beans.BeanUtils;
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
    PlatformConfigService platformConfigService;

    @GetMapping("info")
    @ApiOperation("获取当前用户的基本信息(不包含 余额，打码量，可提现金额,洗码金额)")
    public ResponseEntity<UserVo> info() {
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user,vo);
        vo.setUserId(user.getId());
        //查询后台配置域名
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig != null) {
            vo.setDomain(platformConfig.getDomainNameConfiguration());
        }
        return new ResponseEntity(ResponseCode.SUCCESS, vo);
    }

    @GetMapping("getMoney")
    @ApiOperation("获取当前用户的余额,打码量,可提现金额,洗码金额,分润金额")
    public ResponseEntity<UserVo> getMoney() {
        Long userId = CasinoWebUtil.getAuthId();
        UserVo vo = new UserVo();
        //TODO 查询可提金额，未完成流水(打码量)
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        if (userMoney == null) {
            vo.setUnfinshTurnover(defaultVal);
            vo.setDrawMoney(defaultVal);
            vo.setMoney(defaultVal);
            vo.setWashCode(defaultVal);
            vo.setShareProfit(defaultVal);
            return new ResponseEntity(ResponseCode.SUCCESS, vo);
        }
        BigDecimal money = userMoney.getMoney() == null ? defaultVal : userMoney.getMoney().setScale(2, BigDecimal.ROUND_HALF_UP);
        vo.setMoney(money);
        vo.setDrawMoney(userMoney.getWithdrawMoney());
        BigDecimal codeNum = userMoney.getCodeNum() == null ? defaultVal : userMoney.getCodeNum().setScale(2, BigDecimal.ROUND_HALF_UP);
        vo.setUnfinshTurnover(codeNum);
        BigDecimal washCode= userMoney.getWashCode() == null ? defaultVal : userMoney.getWashCode().setScale(2, BigDecimal.ROUND_HALF_UP);
        vo.setWashCode(washCode);
        BigDecimal shareProfit= userMoney.getShareProfit() == null ? defaultVal : userMoney.getShareProfit().setScale(2, BigDecimal.ROUND_HALF_UP);
        vo.setShareProfit(shareProfit);
        return new ResponseEntity(ResponseCode.SUCCESS, vo);
    }

    @PostMapping("/updateUserInfo")
    @ApiOperation("登录用户修改信息")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "参数类型:0=真实姓名，1=邮箱地址，2=微信账号，3=QQ账号", required = true),
            @ApiImplicitParam(name = "value", required = true),
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
            if(!ObjectUtils.isEmpty(user.getRealName())){
                return ResponseUtil.custom("信息已录入，不允许修改");
            }
            if(ObjectUtils.isEmpty(value)){
                return ResponseUtil.custom("真实姓名不允许为空");
            }
            user.setRealName(value);
        } else if ("1".equals(type)) {
            if(!ObjectUtils.isEmpty(user.getEmail())){
                return ResponseUtil.custom("信息已录入，不允许修改");
            }
            if(ObjectUtils.isEmpty(value)){
                return ResponseUtil.custom("邮箱不允许为空");
            }
            if (!value.matches(RegexEnum.EMAIL.getRegex())) {
                return ResponseUtil.custom("邮箱格式填写错误");
            }
            user.setEmail(value);
        } else if ("2".equals(type)) {
            if(!ObjectUtils.isEmpty(user.getWebChat())){
                return ResponseUtil.custom("信息已录入，不允许修改");
            }
            if(ObjectUtils.isEmpty(value)){
                return ResponseUtil.custom("微信号不允许为空");
            }
            if (!value.matches(RegexEnum.WEBCHAT.getRegex())) {
                return ResponseUtil.custom("微信号"+RegexEnum.WEBCHAT.getDesc());
            }
            user.setWebChat(value);
        } else if ("3".equals(type)) {
            if(!ObjectUtils.isEmpty(user.getQq())){
                return ResponseUtil.custom("信息已录入，不允许修改");
            }
            if(ObjectUtils.isEmpty(value)){
                return ResponseUtil.custom("QQ号不允许为空");
            }
            if (!value.matches(RegexEnum.NUMBER_OR_LETTER.getRegex())) {
                return ResponseUtil.custom("QQ号"+RegexEnum.NUMBER_OR_LETTER.getDesc());
            }
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
        if(ObjectUtils.isEmpty(email)&&ObjectUtils.isEmpty(webChat)&&ObjectUtils.isEmpty(qq)&&ObjectUtils.isEmpty(phone)){
            return ResponseUtil.custom("参数为空");
        }
        if (!ObjectUtils.isEmpty(webChat) && !webChat.matches(RegexEnum.WEBCHAT.getRegex())) {
            return ResponseUtil.custom("微信号"+RegexEnum.WEBCHAT.getDesc());
        }
        if (!ObjectUtils.isEmpty(qq) && !qq.matches(RegexEnum.NUMBER_OR_LETTER.getRegex())) {
            return ResponseUtil.custom("QQ号"+RegexEnum.NUMBER_OR_LETTER.getDesc());
        }
        if (!ObjectUtils.isEmpty(phone) && !phone.matches(RegexEnum.NUMBER_OR_LETTER.getRegex())) {
            return ResponseUtil.custom("手机号"+RegexEnum.NUMBER_OR_LETTER.getDesc());
        }
        if (!ObjectUtils.isEmpty(email) && !email.matches(RegexEnum.EMAIL.getRegex())) {
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

    @GetMapping("everyoneSpread")
    @ApiOperation("获取当前用户人人代推广链接")
    public ResponseEntity everyoneSpread() {
        //查询后台配置域名
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || ObjectUtils.isEmpty(platformConfig.getProxyConfiguration())) {
            return ResponseUtil.custom("推广域名未配置");
        }
        String domain = platformConfig.getProxyConfiguration();
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        if (ObjectUtils.isEmpty(user.getInviteCode())) {
            return ResponseUtil.custom("当前用户邀请码为空");
        }
        String url = domain + "/" + Constants.INVITE_TYPE_EVERYONE + "/" + user.getInviteCode() + "/" + Constants.SPREAD_REGISTER_VIEW;
        return ResponseUtil.success(url);
    }

    public static void main(String[] args) {
        String regex = "^[a-zA-Z]{1}[-_a-zA-Z0-9]{5,19}+$";
        String phone="qweq-11";
        System.out.println(phone.matches(regex));
    }

}
