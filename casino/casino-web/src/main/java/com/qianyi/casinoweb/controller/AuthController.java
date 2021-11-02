package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.code.kaptcha.Producer;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinoweb.runner.GenerateInviteCodeRunner;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.util.DeviceUtil;
import com.qianyi.casinoweb.util.InviteCodeUtil;
import com.qianyi.casinoweb.vo.LoginLogVo;
import com.qianyi.moduleauthenticator.WangyiDunAuthUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.*;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Api(tags = "认证中心")
@RestController
@RequestMapping("auth")
@Slf4j
public class AuthController {

    //这里的captchaProducer要和KaptchaConfig里面的bean命名一样
    @Autowired
    Producer captchaProducer;

    @Autowired
    UserService userService;
    @Autowired
    UserMoneyService userMoneyService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    PlatformConfigService platformConfigService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    IpBlackService ipBlackService;
    @Autowired
    ProxyUserService proxyUserService;
    @Autowired
    GenerateInviteCodeRunner generateInviteCodeRunner;

    @Autowired
    @Qualifier("loginLogJob")
    AsyncService asyncService;
    @Value("${project.smsUrl}")
    private String smsUrl;
    @Value("${project.merchant}")
    private String merchant;

    @PostMapping("spreadRegister")
    @ApiOperation("推广用户注册")
    @NoAuthentication
    @Transactional
    //1分钟3次
//    @RequestLimit(limit = 3, timeout = 60)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "country", value = "区号，柬埔寨：855", required = true),
            @ApiImplicitParam(name = "phone", value = "手机号", required = true),
            @ApiImplicitParam(name = "phoneCode", value = "手机号验证码", required = true),
            @ApiImplicitParam(name = "validate", value = "网易易顿", required = true),
            @ApiImplicitParam(name = "inviteCode", value = "邀请码", required = true),
            @ApiImplicitParam(name = "inviteType", value = "邀请类型:everyone:人人代，proxy:基层代理", required = true),
    })
    public ResponseEntity spreadRegister(String account, String password, String country, String phone, String phoneCode, HttpServletRequest request, String validate, String inviteCode, String inviteType) {
        boolean checkNull = CommonUtil.checkNull(account, password, country, phone, phoneCode, validate, inviteCode, inviteType);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        ResponseEntity checkInviteCode = checkInviteCode(inviteType, inviteCode);
        if (checkInviteCode.getCode() != 0) {
            return checkInviteCode;
        }
        ResponseEntity responseEntity = registerCommon(account, password,country, phone,phoneCode, request, validate, inviteCode, inviteType);
        return responseEntity;
    }

    @PostMapping("register")
    @ApiOperation("前台用户注册")
    @NoAuthentication
    @Transactional
    //1分钟3次
//    @RequestLimit(limit = 3, timeout = 60)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "country", value = "区号，柬埔寨：855", required = true),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = true),
            @ApiImplicitParam(name = "phoneCode", value = "手机号验证码", required = true),
            @ApiImplicitParam(name = "validate", value = "网易易顿", required = true),
            @ApiImplicitParam(name = "inviteCode", value = "邀请码", required = false),
    })
    public ResponseEntity register(String account, String password, String country, String phone, String phoneCode,
                                   HttpServletRequest request, String validate, String inviteCode) {
        boolean checkNull = CommonUtil.checkNull(account, password, country, phone, phoneCode, validate);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || platformConfig.getRegisterSwitch() == null || platformConfig.getRegisterSwitch() == Constants.close) {
            return ResponseUtil.custom("注册通道已关闭");
        }
        ResponseEntity responseEntity = registerCommon(account, password, country, phone, phoneCode, request, validate, inviteCode, null);
        return responseEntity;
    }

    /**
     * 注册公共逻辑
     * @param account
     * @param password
     * @param phone
     * @param request
     * @param validate
     * @param inviteCode
     * @param inviteType
     * @return
     */
    @Transactional
    public ResponseEntity registerCommon(String account, String password, String country, String phone, String phoneCode,
                                         HttpServletRequest request, String validate, String inviteCode, String inviteType) {
        boolean wangyidun = WangyiDunAuthUtil.verify(validate);
        if (!wangyidun) {
            return ResponseUtil.custom("验证码错误");
        }
        //卫语句校验
        boolean checkAccountLength = User.checkAccountLength(account);
        if (!checkAccountLength) {
            return ResponseUtil.custom("用户名" + RegexEnum.ACCOUNT.getDesc());
        }
        boolean checkPasswordLength = User.checkPasswordLength(password);
        if (!checkPasswordLength) {
            return ResponseUtil.custom("密码" + RegexEnum.ACCOUNT.getDesc());
        }
        boolean checkPhone = User.checkPhone(phone);
        if (!checkPhone) {
            return ResponseUtil.custom("手机号" + RegexEnum.PHONE.getDesc());
        }
        phone = country + phone;
        String redisKey = Constants.REDIS_SMSCODE + phone;
        Object redisCode = redisUtil.get(redisKey);
        if (!phoneCode.equals(redisCode)) {
             return ResponseUtil.custom("手机号验证码错误");
        }
        //一个手机号只能注册一个账号
        List<User> phoneUser = userService.findByPhone(phone);
        if (!CollectionUtils.isEmpty(phoneUser)) {
//            return ResponseUtil.custom("当前手机号已注册");
        }
        String ip = IpUtil.getIp(request);
        //查询ip注册账号限制
        PlatformConfig platformConfig = platformConfigService.findFirst();
        Integer timeLimit = null;
        if (platformConfig != null) {
            timeLimit = platformConfig.getIpMaxNum() == null ? 5 : platformConfig.getIpMaxNum();
        }
        Integer count = userService.countByIp(ip);
        if (count != null && count > timeLimit) {
            return ResponseUtil.custom("当前IP注册帐号数量超过上限");
        }

        User user = userService.findByAccount(account);
        if (user != null && !CommonUtil.checkNull(user.getPassword())) {
            return ResponseUtil.custom("该帐号已存在");
        }
        //设置user基本参数
        String inviteCodeNew = generateInviteCodeRunner.getInviteCode();
        user = User.setBaseUser(account, CasinoWebUtil.bcrypt(password), phone, ip,inviteCodeNew);
        //设置父级
        setParent(inviteCode, inviteType, user);
        User save = userService.save(user);
        //userMoney表初始化数据
        UserMoney userMoney = new UserMoney();
        userMoney.setUserId(save.getId());
        userMoneyService.save(userMoney);
        //记录注册日志
        setLoginLog(ip, user, request);
        //推送MQ
        sendUserMq(save);
        return ResponseUtil.success();
    }

    /**
     * 记录注册日志
     * @param ip
     * @param user
     * @param request
     */
    public void setLoginLog(String ip,User user,HttpServletRequest request){
        LoginLogVo vo = new LoginLogVo();
        vo.setIp(ip);
        vo.setAccount(user.getAccount());
        vo.setUserId(user.getId());
        //检测请求设备
        String ua = request.getHeader("User-Agent");
        boolean checkMobileOrPc = DeviceUtil.checkAgentIsMobile(ua);
        if(checkMobileOrPc){
            vo.setRemark("Mobile");
        }else{
            vo.setRemark("PC");
        }
        vo.setType(2);
        asyncService.executeAsync(vo);
    }

    /**
     * 推送团队新增成员MQ
     * @param user
     */
    public void sendUserMq(User user){
        log.info("开始推送团队新增成员消息", user);
        rabbitTemplate.convertAndSend(RabbitMqConstants.ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.ADDUSERTOTEAM_DIRECT, user, new CorrelationData(UUID.randomUUID().toString()));
        log.info("团队新增成员消息发送成功={}", user);
    }

    /**
     * 设置父级或者父级代理
     * @param inviteCode
     * @param inviteType
     * @param user
     */
    public void setParent(String inviteCode,String inviteType,User user){
        //人人代
        if(Constants.INVITE_TYPE_EVERYONE.equals(inviteType)){
            User parentUser = userService.findByInviteCode(inviteCode);
            user.setFirstPid(parentUser.getId());
            user.setSecondPid(parentUser.getFirstPid());
            user.setThirdPid(parentUser.getSecondPid());
            user.setType(Constants.USER_TYPE0);
            //基层代理
        }else if(Constants.INVITE_TYPE_PROXY.equals(inviteType)){
            ProxyUser parentProxy = proxyUserService.findByProxyCode(inviteCode);
            user.setFirstProxy(parentProxy.getFirstProxy());
            user.setSecondProxy(parentProxy.getSecondProxy());
            user.setThirdProxy(parentProxy.getId());
            user.setType(Constants.USER_TYPE1);
            //前台自己注册
        }else{
            user.setType(Constants.USER_TYPE0);
            user.setFirstPid(0L);//默认公司级别
            User parentUser = null;
            if (!ObjectUtils.isEmpty(inviteCode)) {
                parentUser = userService.findByInviteCode(inviteCode);
            }
            if (parentUser != null) {
                user.setFirstPid(parentUser.getId());
                user.setSecondPid(parentUser.getFirstPid());
                user.setThirdPid(parentUser.getSecondPid());
            }
        }
    }

    @NoAuthentication
    @ApiOperation("帐密登陆.谷歌验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "validate", value = "网易易顿", required = true),
            @ApiImplicitParam(name = "deviceId", value = "设备ID,移动端必传", required = false),
    })
    @PostMapping("loginA")
    public ResponseEntity loginA(
            String account,
            String password,
            String validate,String deviceId,HttpServletRequest request) {
        if (CasinoWebUtil.checkNull(account, password, validate)) {
            return ResponseUtil.parameterNotNull();
        }

//        //验证码校验
//        boolean captcha = CasinoWebUtil.checkCaptcha(captchaCode, captchaText);
//        if (!captcha) {
//            return ResponseUtil.custom("验证码错误");
//        }

        User user = userService.findByAccount(account);
        if (user == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }
        if(!User.checkUser(user)){
            return ResponseUtil.custom("账号被封");
        }
        String bcryptPassword = user.getPassword();
        boolean bcrypt = CasinoWebUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }
        //常用设备优先校验
        boolean verifyFlag = false;
        if (!ObjectUtils.isEmpty(user.getDeviceId()) && !ObjectUtils.isEmpty(deviceId) && user.getDeviceId().equals(deviceId)) {
            verifyFlag = true;
        }
        if (!verifyFlag) {
            //验证码校验
            boolean wangyidun = WangyiDunAuthUtil.verify(validate);
            if (!wangyidun) {
                return ResponseUtil.custom("验证码错误");
            }
        }
        if (ObjectUtils.isEmpty(user.getDeviceId()) && !ObjectUtils.isEmpty(deviceId)) {
            user.setDeviceId(deviceId);
            userService.save(user);
        }
        //记录登陆日志
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        LoginLogVo vo = new LoginLogVo();
        vo.setIp(ip);
        vo.setAccount(user.getAccount());
        vo.setUserId(user.getId());
        //检测请求设备
        String ua = request.getHeader("User-Agent");
        boolean checkMobileOrPc = DeviceUtil.checkAgentIsMobile(ua);
        if(checkMobileOrPc){
            vo.setRemark("Mobile");
        }else{
            vo.setRemark("PC");
        }
        vo.setType(1);
        asyncService.executeAsync(vo);

        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(String.valueOf(user.getId()));
        subject.setBcryptPassword(user.getPassword());
        String token = JjwtUtil.generic(subject,Constants.CASINO_WEB);
        setUserTokenToRedis(user.getId(), token);
        return ResponseUtil.success(token);
    }

//    @NoAuthentication
//    @ApiOperation("帐密登陆.谷歌身份验证器")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "account", value = "帐号", required = true),
//            @ApiImplicitParam(name = "password", value = "密码", required = true),
//            @ApiImplicitParam(name = "code", value = "验证码", required = true),
//    })
//    @PostMapping("loginB")
//    public ResponseEntity loginB(String account, String password, Integer code) {
//        if (ObjectUtils.isEmpty(account) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(code)) {
//            return ResponseUtil.parameterNotNull();
//        }
//
//        boolean length = User.checkLength(account, password);
//        if (!length) {
//            return ResponseUtil.custom("帐号,密码长度3-15位");
//        }
//
//        User user = userService.findByAccount(account);
//        if (user == null) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        String bcryptPassword = user.getPassword();
//        boolean bcrypt = PayUtil.checkBcrypt(password, bcryptPassword);
//        if (!bcrypt) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        boolean flag = User.checkUser(user);
//        if (!flag) {
//            return ResponseUtil.custom("该帐号不可操作");
//        }
//
//        String secret = user.getSecret();
//        if (PayUtil.checkNull(secret)) {
//            return ResponseUtil.custom("请先绑定谷歌身份验证器");
//        }
//        boolean checkCode = GoogleAuthUtil.check_code(secret, code);
//        if (!checkCode) {
//            return ResponseUtil.googleAuthNoPass();
//        }
//
//        String token = JjwtUtil.generic(user.getId() + "");
//
//        //记录登陆日志
//        String ip = IpUtil.getIp(PayUtil.getRequest());
//        new Thread(new LoginLogJob(ip, user.getAccount(), user.getId(), "admin")).start();
//
//        return ResponseUtil.success(token);
//    }


    @ApiOperation("谷歌图形验证码")
    @ApiImplicitParam(name = "code", value = "code前端可随机数或者时间戮，以降低冲突的次数", required = true)
    @GetMapping("captcha")
    @NoAuthentication
    public void captcha(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (CommonUtil.checkNull(code)) {
            return;
        }
        //生产验证码字符串并保存到session中
        String createText = captchaProducer.createText();

        String key = CasinoWebUtil.getCaptchaKey(request, code);
        ExpiringMapUtil.putMap(key, createText);

        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        ServletOutputStream responseOutputStream = response.getOutputStream();

        //使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
        BufferedImage challenge = captchaProducer.createImage(createText);
        ImageIO.write(challenge, "jpg", jpegOutputStream);
        byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();
    }
//
//    @GetMapping("google/auth/bind")
//    @NoAuthentication
//    @ApiOperation("绑定谷歌身份验证器")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "account", value = "帐号", required = true),
//            @ApiImplicitParam(name = "password", value = "密码", required = true)
//    })
//    @ApiResponses({
//            @ApiResponse(code = 0, message = "返回二维码地址")
//    })
//    public ResponseEntity bindGoogleAuth(String account, String password) {
//        if (PayUtil.checkNull(account) || PayUtil.checkNull(password)) {
//            return ResponseUtil.parameterNotNull();
//        }
//
//        User user = userService.findByAccount(account);
//        if (user == null) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        String bcryptPassword = user.getPassword();
//        boolean bcrypt = PayUtil.checkBcrypt(password, bcryptPassword);
//        if (!bcrypt) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        boolean flag = User.checkUser(user);
//        if (!flag) {
//            return ResponseUtil.custom("该帐号不可操作");
//        }
//
//        String secret = user.getSecret();
//
//        if (PayUtil.checkNull(secret)) {
//            secret = GoogleAuthUtil.generateSecretKey();
//            userService.setSecretById(user.getId(), secret);
//        }
//
//        String qrcode = GoogleAuthUtil.getQcode(account, secret);
//        return ResponseUtil.success(qrcode);
//
//    }

    @GetMapping("getJwtToken")
    @ApiOperation("开发者通过此令牌调试接口。不可用于正式请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "账号。", required = true),
    })
    @NoAuthentication
    public ResponseEntity getJwtToken(String account) {
        User user = userService.findByAccount(account);
        if (user == null) {
            return ResponseUtil.custom("账号不存在");
        }
        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(String.valueOf(user.getId()));
        subject.setBcryptPassword(user.getPassword());
        String jwt = JjwtUtil.generic(subject,Constants.CASINO_WEB);
        setUserTokenToRedis(user.getId(), jwt);
        return ResponseUtil.success(jwt);
    }

    @GetMapping("getRegisterStatus")
    @ApiOperation("查询注册通道状态")
    @NoAuthentication
    public ResponseEntity getRegisterStatus() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || platformConfig.getRegisterSwitch() == null) {
            return ResponseUtil.success(Constants.close);
        }
        return ResponseUtil.success(platformConfig.getRegisterSwitch());
    }

    @PostMapping("rjt")
    @ApiOperation("JWT过期后，30分钟内可颁发新的token")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "旧TOKEN", required = true),
    })
    public ResponseEntity refreshJwtToken(String token) {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId(token);
        if (authId == null) {
            return ResponseUtil.authenticationNopass();
        }
        User user = userService.findById(authId);
        String refreshToken = JjwtUtil.refreshToken(token, user.getPassword(),Constants.CASINO_WEB);
        if (ObjectUtils.isEmpty(refreshToken)) {
            return ResponseUtil.authenticationNopass();
        }
        setUserTokenToRedis(authId, refreshToken);
        return ResponseUtil.success(refreshToken);
    }

    @GetMapping("checkInviteCode")
    @ApiOperation("校验邀请码")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "inviteType", value = "邀请类型:everyone:人人代，proxy:基层代理", required = true),
            @ApiImplicitParam(name = "inviteCode", value = "邀请码", required = true),
    })
    public ResponseEntity checkInviteCode(String inviteType, String inviteCode) {
        boolean checkNull = CommonUtil.checkNull(inviteType, inviteCode);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        if (Constants.INVITE_TYPE_EVERYONE.equals(inviteType)) {
            User user = userService.findByInviteCode(inviteCode);
            if (user != null) {
                return ResponseUtil.success();
            }
            String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
            IpBlack ipBlack = new IpBlack();
            ipBlack.setIp(ip);
            ipBlack.setStatus(Constants.no);
            ipBlack.setRemark("人人代邀请码填写错误，IP被封");
            ipBlackService.save(ipBlack);
            return ResponseUtil.custom(Constants.IP_BLOCK);
        } else if (Constants.INVITE_TYPE_PROXY.equals(inviteType)) {
            ProxyUser proxyUser = proxyUserService.findByProxyCode(inviteCode);
            if (proxyUser != null) {
                return ResponseUtil.success();
            }
            String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
            IpBlack ipBlack = new IpBlack();
            ipBlack.setIp(ip);
            ipBlack.setStatus(Constants.no);
            ipBlack.setRemark("基层代理邀请码填写错误，IP被封");
            ipBlackService.save(ipBlack);
            return ResponseUtil.custom(Constants.IP_BLOCK);
        }
        return ResponseUtil.custom("邀请码检验失败");
    }

    @GetMapping("getVerificationCode")
    @ApiOperation("通过手机号获取验证码")
    @NoAuthentication
    @RequestLimit(limit = 1, timeout = 50)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "country", value = "区号，柬埔寨：855", required = true),
            @ApiImplicitParam(name = "phone", value = "手机号", required = true)
    })
    public ResponseEntity getVerificationCode(String country, String phone) {
        boolean checkNull = CommonUtil.checkNull(country, phone);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        String regex = "^[0-9]*[1-9][0-9]*$";
        if (!country.matches(regex) || !phone.matches(regex)) {
            return ResponseUtil.custom("区号和手机号必须是纯数字");
        }
        //每日ip发送短信数量限制为10条
        String today = DateUtil.dateToyyyyMMdd(new Date());
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        String todayIpKey = Constants.REDIS_SMSIPSENDNUM + today + "::" + ip;
        Object todayIpNum = redisUtil.get(todayIpKey);
        if (todayIpNum != null && (int) todayIpNum >= 10) {
            return ResponseUtil.custom("当前IP今日获取验证码次数已达上限");
        }
        String phoneKey = Constants.REDIS_SMSCODE + country + phone;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("merchant", merchant);
        paramMap.put("country", country);
        paramMap.put("phone", phone);
        Integer language = 1;
        if ("855".equals(country)) {
            language = 3;
        }
        paramMap.put("language", language);
        String code = InviteCodeUtil.randomNumCode(6);
        paramMap.put("code", code);
        String response = HttpClient4Util.doPost(smsUrl + "/buka/sendRegister", paramMap);
        if (CommonUtil.checkNull(response)) {
            return ResponseUtil.custom("获取验证码失败,请重新操作");
        }
        ResponseEntity responseEntity = JSONObject.parseObject(response, ResponseEntity.class);
        if (responseEntity.getCode() != ResponseCode.SUCCESS.getCode()) {
            return responseEntity;
        }
        //验证码有效期为5分钟
        redisUtil.set(phoneKey, code, 60 * 5);
        //ip每天发送短信数量限制加1
        if (todayIpNum == null) {
            redisUtil.set(todayIpKey, 1, 60 * 60 * 24);
        } else {
            redisUtil.incr(todayIpKey, 1);
        }
        return ResponseUtil.success();
    }

    private void setUserTokenToRedis(Long userId, String token) {
        redisUtil.set("token:" + userId, token);
    }
}
