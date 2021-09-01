package com.qianyi.casinoweb.controller;

import com.google.code.kaptcha.Producer;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.job.LoginLogJob;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.ExpiringMapUtil;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Api(tags = "认证中心")
@RestController
@RequestMapping("auth")
public class AuthController {

    //这里的captchaProducer要和KaptchaConfig里面的bean命名一样
    @Autowired
    Producer captchaProducer;

    @Autowired
    UserService userService;

    @PostMapping("register")
    @ApiOperation("用户注册")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = true),
            @ApiImplicitParam(name = "captchaCode", value = "验证码代号", required = true),
            @ApiImplicitParam(name = "captchaText", value = "验证码文本", required = true),
    })
    public ResponseEntity register(String account, String password, String phone,
                                   HttpServletRequest request, String captchaCode,
                                   String captchaText) {
        boolean checkNull = CommonUtil.checkNull(account, password, phone, captchaCode, captchaText);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        //验证码校验
        boolean captcha = CasinoWebUtil.checkCaptcha(captchaCode, captchaText);
        if (!captcha) {
            return ResponseUtil.custom("验证码错误");
        }

        //卫语句校验
        boolean checkAccountLength = User.checkAccountLength(account);
        if (!checkAccountLength) {
            return ResponseUtil.custom("用户名长度6-15位,由字母，数字，下划线组成");
        }

        boolean checkPasswordLength = User.checkPasswordLength(password);
        if (!checkPasswordLength) {
            return ResponseUtil.custom("密码长度6-15位,由字母，数字，下划线组成");
        }

        User user = userService.findByAccount(account);
        if (user != null && !CommonUtil.checkNull(user.getPassword())) {
            return ResponseUtil.custom("该帐号已存在");
        }

        user = new User();
        user.setAccount(account);
        user.setPassword(CasinoWebUtil.bcrypt(password));
        user.setPhone(phone);
        user.setState(Constants.open);
        String ip = IpUtil.getIp(request);
        user.setRegisterIp(ip);

        userService.save(user);
        return ResponseUtil.success();
    }

    @NoAuthentication
    @ApiOperation("帐密登陆.谷歌验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "captchaCode", value = "验证码代号", required = true),
            @ApiImplicitParam(name = "captchaText", value = "验证码文本", required = true),
    })
    @PostMapping("loginA")
    public ResponseEntity loginA(
            String account,
            String password,
            String captchaCode,
            String captchaText) {
        if (ObjectUtils.isEmpty(account) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(captchaCode) || ObjectUtils.isEmpty(captchaText)) {
            return ResponseUtil.parameterNotNull();
        }

        //验证码校验
        boolean captcha = CasinoWebUtil.checkCaptcha(captchaCode, captchaText);
        if (!captcha) {
            return ResponseUtil.custom("验证码错误");
        }

        User user = userService.findByAccount(account);
        if (user == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        String bcryptPassword = user.getPassword();
        boolean bcrypt = CasinoWebUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        boolean flag = User.checkUser(user);
        if (!flag) {
            return ResponseUtil.custom("该帐号不可操作");
        }

        //记录登陆日志
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        new Thread(new LoginLogJob(ip, user.getAccount(), user.getId(), "casino-web")).start();


        String token = JjwtUtil.generic(user.getId() + "");
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
            @ApiImplicitParam(name = "token", value = "固定值。", required = true),
    })
    @NoAuthentication
    public ResponseEntity getJwtToken(String token) {
        if (!("dashan".equals(token) || "xiaoxiannv".equals(token)) || "xiaoben".equals(token)) {
            return ResponseUtil.custom("找管理员拿token");
        }
        String jwt = JjwtUtil.generic("1");
        return ResponseUtil.success(jwt);
    }

    @PostMapping("rjt")
    @ApiOperation("JWT过期后，30分钟内可颁发新的token")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "旧TOKEN", required = true),
    })
    public ResponseEntity refreshJwtToken(String token) {
        String refreshToken = JjwtUtil.refreshToken(token);
        if (ObjectUtils.isEmpty(refreshToken)) {
            return ResponseUtil.authenticationNopass();
        }

        return ResponseUtil.success(refreshToken);
    }
}
