package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.service.OrderService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.service.WithdrawOrderService;
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
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.math.BigDecimal;

/**
 * 提现记录表
 */
@Slf4j
@RestController
@RequestMapping("/withdraw")
@Api(tags = "资金中心")
public class WithdrawOrderController {

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private WithdrawBusiness withdrawBusiness;

    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserMoneyService userMoneyService;

    @ApiOperation("提现列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "订单状态", required = false),
            @ApiImplicitParam(name = "no", value = "订单号", required = false),
            @ApiImplicitParam(name = "bankId", value = "银行卡Id", required = false),
    })
    @GetMapping("/withdrawList")
    public ResponseEntity withdrawList(Integer pageSize,Integer pageCode, Integer status, String no, String bankId){
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        if(status != null){
            withdrawOrder.setStatus(status);
        }
        if(!LoginUtil.checkNull(no)){
            withdrawOrder.setNo(no);
        }
        if(!LoginUtil.checkNull(bankId)){
            withdrawOrder.setBankId(bankId);
        }
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<WithdrawOrder> withdrawOrderPage = withdrawOrderService.findUserPage(pageable, withdrawOrder);
        return ResponseUtil.success(withdrawOrderPage);
    }

    @ApiOperation("提现审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", required = true),
            @ApiImplicitParam(name = "status", value = "审核状态，1：通过，2：拒绝，3：其他", required = true),
    })
    @PostMapping("saveWithdraw")
    public ResponseEntity saveWithdraw(Long id, Integer status){
        if(status != CommonConst.NUMBER_1 && status != CommonConst.NUMBER_2 && status != CommonConst.NUMBER_3){
            return ResponseUtil.custom("参数不合法");
        }
        return withdrawBusiness.updateWithdrawAndUser(id,status);
    }
    @ApiOperation("模拟提现")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
            @ApiImplicitParam(name = "bigDecimal", value = "提现金额", required = true),
    })
    @GetMapping("save")
    public ResponseEntity save(Long id,BigDecimal bigDecimal){
        UserMoney byUserId = userMoneyService.findByUserId(id);
        if (byUserId.getMoney().compareTo(bigDecimal)<=0){
            return ResponseUtil.custom("余额不足");
        }
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        withdrawOrder.setWithdrawMoney(bigDecimal);
        withdrawOrder.setBankId("156464168464654646");
        withdrawOrder.setUserId(id);
        withdrawOrder.setNo(orderService.getOrderNo());
        withdrawOrder.setStatus(0);
        withdrawOrderService.saveOrder(withdrawOrder);
        userMoneyService.subMoney(id,bigDecimal);
        return ResponseUtil.success();
    }

}
