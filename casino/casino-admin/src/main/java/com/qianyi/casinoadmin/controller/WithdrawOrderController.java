package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.WithdrawOrder;
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

/**
 * 提现记录表
 */
@Slf4j
@RestController
@RequestMapping("/withdraw")
@Api(tags = "资金管理")
public class WithdrawOrderController {

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private UserService userService;

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
        if(LoginUtil.checkNull(no)){
            withdrawOrder.setNo(no);
        }
        if(LoginUtil.checkNull(bankId)){
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
        if(status <= 0 || status > 3){
            return ResponseUtil.custom("参数不合法");
        }
        WithdrawOrder withdrawOrder = withdrawOrderService.findById(id);
        if(withdrawOrder == null){
            return ResponseUtil.custom("订单不存在");
        }
        //提现通过或其他
        withdrawOrder.setStatus(status);
        if(status == Constants.WITHDRAW_PASS || status == Constants.WITHDRAW_ORDER){
            withdrawOrderService.saveOrder(withdrawOrder);
        }
        //提现拒绝，钱要退回给用户
        if(status == Constants.WITHDRAW_REFUSE){
            return updateWithdrawAndUser(withdrawOrder);
        }
        return ResponseUtil.fail();
    }

    @Transactional
    public ResponseEntity updateWithdrawAndUser(WithdrawOrder withdrawOrder) {
        Long userId = withdrawOrder.getUserId();
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseUtil.custom("用户不存在");
        }
        user.setWithdrawMoney(user.getWithdrawMoney().add(withdrawOrder.getWithdrawMoney()));
        WithdrawOrder withdraw = withdrawOrderService.saveOrder(withdrawOrder);
        log.info("user sum money is {}, add withdrawMoney is {}",user.getWithdrawMoney(), withdrawOrder.getWithdrawMoney());
        User save = userService.save(user);
        return ResponseUtil.success(withdraw);
    }
}
