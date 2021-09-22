package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.util.passwordUtil;
import com.qianyi.casinocore.business.ChargeOrderBusiness;
import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 对用户表进行增删改查操作
 */
@RestController
@RequestMapping("user")
@Api(tags = "客户中心")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ChargeOrderBusiness chargeOrderBusiness;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private WithdrawBusiness withdrawBusiness;

    @Autowired
    UserThirdService userThirdService;

    @Autowired
    PublicWMApi wmApi;

    /**
     * 查询操作
     * 注意：jpa 是从第0页开始的
     * @return
     */
    @ApiOperation("用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "account", value = "用户名", required = false),
            @ApiImplicitParam(name = "state", value = "1：启用，其他：禁用", required = false),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = false),
    })
    @GetMapping("findUserList")
    public ResponseEntity findUserList(Integer pageSize, Integer pageCode, String account,Integer state,
                                       @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
                                       @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){

        //后续扩展加参数。
        User user = new User();
        user.setAccount(account);
        user.setState(state);
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<User> userPage = userService.findUserPage(pageable, user,startDate,endDate);
        List<User> userList = userPage.getContent();
        if(userList != null && userList.size() > 0){
            List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
            List<UserMoney> userMoneyList =  userMoneyService.findAll(userIds);
            if(userMoneyList != null && userMoneyList.size() > 0){
                userList.stream().forEach(u -> {
                    WithdrawOrder withdrawOrder = new WithdrawOrder();
                    withdrawOrder.setStatus(CommonConst.NUMBER_3);
                    withdrawOrder.setUserId(u.getId());
                    List<WithdrawOrder> orderList = withdrawOrderService.findOrderList(withdrawOrder);
                    BigDecimal freezeMoney = orderList.stream().map(WithdrawOrder::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
                    u.setFreezeMoney(freezeMoney);//冻结余额
                    userMoneyList.stream().forEach(userMoney -> {
                        if(u.getId().equals(userMoney.getUserId())){
                            u.setMoney(userMoney.getMoney());
                            u.setCodeNum(userMoney.getCodeNum());
                            u.setWithdrawMoney(userMoney.getWithdrawMoney());//可以提现金额
                        }
                    });
                });
            }
            this.setWMMoney(userList);
        }
        return ResponseUtil.success(userPage);
    }

    public void setWMMoney(List<User> userList) {

        log.info("query WM money data：【{}】 ", userList);
        List<CompletableFuture<User>> completableFutures = new ArrayList<>();
        for (User user : userList) {
            UserThird third = userThirdService.findByUserId(user.getId());
            if (third == null) {
                continue;
            }
            CompletableFuture<User> completableFuture =  CompletableFuture.supplyAsync(() -> {
                return getWMonetUser(user, third);
            });
            completableFutures.add(completableFuture);
        }
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{}));
        try {
            voidCompletableFuture.join();
        }catch (Exception e){
            //打印日志
            log.error("query User WM money error：【{}】", e);
        }
    }

    private User getWMonetUser(User user, UserThird third) {
        Integer lang = user.getLanguage();
        if (lang == null) {
            lang = 0;
        }
        BigDecimal balance = null;
        try {
            balance = wmApi.getBalance(third.getAccount(), lang);
        } catch (Exception e) {
            e.printStackTrace();
        }
        user.setWmMoney(balance);
        return user;
    }

    @ApiOperation("添加用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "用户名", required = true),
            @ApiImplicitParam(name = "name", value = "用户昵称", required = false),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = false),
    })
    @PostMapping("saveUser")
    public ResponseEntity saveUser(String account, String name, String phone){
        User us = userService.findByAccount(account);
        if(us != null){
            return ResponseUtil.custom("账户已存在");
        }

        User user = new User();
        user.setAccount(account);
        if(LoginUtil.checkNull(name)){
            user.setName(account);
        }else{
            user.setName(name);
        }

        user.setState(Constants.open);

        if(!LoginUtil.checkNull(phone)){
            user.setPhone(phone);
        }

        //默认中文
        user.setLanguage(Constants.USER_LANGUAGE_CH);

        //随机生成
        String password = passwordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
        user.setPassword(bcryptPassword);

        User save = userService.save(user);
        //userMoney表初始化数据
        UserMoney userMoney=new UserMoney();
        userMoney.setUserId(save.getId());
        userMoney.setMoney(BigDecimal.ZERO);
        userMoney.setCodeNum(BigDecimal.ZERO);
        userMoneyService.save(userMoney);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", account);
        jsonObject.put("password", password);
        return ResponseUtil.success(jsonObject);
    }

    /**
     * 修改用户
     * 只有修改电话功能，那电话不能为空
     *
     * @param id
     * @param state
     * @param phone
     * @return
     */
    @ApiOperation("修改用户电话")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = true),

    })
    @PostMapping("updateUser")
    public ResponseEntity updateUser(Long id, Integer state, String phone){
        //权限功能会过滤权限,此处不用

        //查询用户信息
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        user.setPhone(phone);
        userService.save(user);
        return ResponseUtil.success();
    }

//    @ApiOperation("删除用户")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "用户id", required = true),
//    })
//    @PostMapping("deteleUser")
//    public ResponseEntity deteleUser(Long id){
//        User user = userService.findById(id);
//        if(user == null){
//            return ResponseUtil.custom("账户不存在");
//        }
//        userService.deleteById(id);
//        return ResponseUtil.success();
//    }

    @ApiOperation("修改用户状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("updateUserStatus")
    public ResponseEntity updateUserStatus(Long id){
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        //开启状态，冻结
        if(user.getState() == Constants.USER_NORMAL){
            user.setState(Constants.USER_LOCK_ACCOUNT);
        }else{
            user.setState(Constants.USER_NORMAL);
        }
        userService.save(user);
        return ResponseUtil.success();
    }

    @ApiOperation("重置用户提现密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("withdrawPassword")
    public ResponseEntity withdrawPassword(Long id){
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        //随机生成
        String withdrawPassword = passwordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(withdrawPassword);
        user.setWithdrawPassword(bcryptPassword);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", user.getAccount());
        jsonObject.put("withdrawPassword", withdrawPassword);
        return ResponseUtil.success(jsonObject);
    }

    @ApiOperation("重置用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("resetPassword")
    public ResponseEntity resetPassword(Long id){
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        //随机生成
        String password = passwordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
        user.setPassword(bcryptPassword);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", user.getAccount());
        jsonObject.put("password", password);
        return ResponseUtil.success(jsonObject);
    }

    /**
     * 后台新增充值订单
     *
     * @param id 会员id
     * @param remitter 汇款人姓名
     * @param chargeAmount 汇款金额
     * @param remark 汇款备注
     * @return
     */
    @ApiOperation("后台新增充值订单 上分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "会员id", required = true),
            @ApiImplicitParam(name = "remitter", value = "汇款人姓名", required = true),
            @ApiImplicitParam(name = "chargeAmount", value = "汇款金额", required = true),
            @ApiImplicitParam(name = "remark", value = "汇款备注", required = false),
    })
    @PostMapping("/saveChargeOrder")
    public ResponseEntity saveChargeOrder(Long id,String remitter,String remark, BigDecimal chargeAmount){
        if (id == null || chargeAmount == null){
            return ResponseUtil.custom("参数不合法");
        }
        if (chargeAmount.compareTo(new BigDecimal(CommonConst.NUMBER_100)) >= CommonConst.NUMBER_1){
            return ResponseUtil.custom("测试环境加钱不能超过100RMB");
        }
        ChargeOrder chargeOrder = new ChargeOrder();
        chargeOrder.setUserId(id);
        chargeOrder.setRemitter(remitter);
        chargeOrder.setRemark(remark);
        chargeOrder.setRemitType(CommonConst.NUMBER_1);
        chargeOrder.setOrderNo(orderService.getOrderNo());
        chargeOrder.setChargeAmount(chargeAmount);
        chargeOrder.setType(CommonConst.NUMBER_2);//管理员新增
        chargeOrder.setStatus(CommonConst.NUMBER_1);
        return chargeOrderBusiness.saveOrderSuccess(chargeOrder);
    }
    /**
     * 后台新增提现订单
     *
     * @param id 会员id
     * @param withdrawMoney 提现金额
     * @param bankId 银行id
     * @return
     */
    @ApiOperation("后台新增提现订单 下分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
            @ApiImplicitParam(name = "withdrawMoney", value = "提现金额", required = true),
            @ApiImplicitParam(name = "bankId", value = "银行id", required = true),
    })
    @PostMapping("/saveWithdrawOrder")
    public ResponseEntity saveWithdrawOrder(Long id,BigDecimal withdrawMoney,String bankId){
        if (id == null || withdrawMoney == null|| bankId == null){
            return ResponseUtil.custom("参数不合法");
        }
        return withdrawBusiness.updateWithdrawAndUser(id,withdrawMoney,bankId);
    }
    /**
     * 查询操作
     * 注意：jpa 是从第0页开始的
     @param tag tag 反差类型 0 ip地址 1 银行卡号
     @param context 搜索内容
     * @return
     */
    @ApiOperation("客户反查")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tag", value = "tag 反差类型 0 ip地址 1 银行卡号", required = true),
            @ApiImplicitParam(name = "context", value = "context 搜索内容", required = true),
    })
    @GetMapping("findUserPegging")
    public ResponseEntity findUserPegging(Integer tag,String context){
        if (tag == CommonConst.NUMBER_0){//反查ip
            User user = new User();
            user.setRegisterIp(context);
            List<User> userList = userService.findUserList(user);
            List<LoginLog> loginLogList = loginLogService.findLoginLogGroupBy(context);
            Map<String,Object> map = new HashMap<>();
            map.put("register",userList);
            map.put("login",loginLogList);
            return ResponseUtil.success(map);
        }else if(tag == CommonConst.NUMBER_1){//反查银行卡号
            Bankcards bankcards = new Bankcards();
            bankcards.setBankAccount(context);
            List<Bankcards> userBank = bankcardsService.findUserBank(bankcards);
            return ResponseUtil.success(userBank);
        }
        return ResponseUtil.fail();
    }
}
