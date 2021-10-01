package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.PageResultVO;
import com.qianyi.casinoadmin.vo.WithdrawOrderVo;
import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.service.OrderService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.service.WithdrawOrderService;
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
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    private OrderService orderService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private UserService userService;

    @ApiOperation("提现列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "订单状态", required = false),
            @ApiImplicitParam(name = "no", value = "订单号", required = false),
            @ApiImplicitParam(name = "bankId", value = "银行卡Id", required = false),
            @ApiImplicitParam(name = "account", value = "用户账号", required = false),
    })
    @GetMapping("/withdrawList")
    public ResponseEntity withdrawList(Integer pageSize,Integer pageCode, Integer status, String account, String no, String bankId){
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        if (!LoginUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            withdrawOrder.setUserId(user.getId());
        }
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
        PageResultVO<WithdrawOrderVo> pageResultVO = new PageResultVO(withdrawOrderPage);
        List<WithdrawOrder> content = withdrawOrderPage.getContent();
        if(content != null && content.size() > 0){
            List<WithdrawOrderVo> withdrawOrderVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(WithdrawOrder::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null && userList.size() > 0){
                content.stream().forEach(withdraw ->{
                    WithdrawOrderVo withdrawOrderVo = new WithdrawOrderVo(withdraw);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(withdraw.getUserId())){
                            withdrawOrderVo.setAccount(user.getAccount());
                        }
                    });
                    withdrawOrderVoList.add(withdrawOrderVo);
                });
            }
            pageResultVO.setContent(withdrawOrderVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("提现审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", required = true),
            @ApiImplicitParam(name = "status", value = "审核状态，1：通过，2：拒绝", required = true),
    })
    @PostMapping("saveWithdraw")
    public ResponseEntity saveWithdraw(Long id, Integer status){
        if (LoginUtil.checkNull(id,status)){
            return ResponseUtil.custom("参数不合法");
        }
        if(status != CommonConst.NUMBER_1 && status != CommonConst.NUMBER_2){
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
        if (LoginUtil.checkNull(id,bigDecimal)){
            return ResponseUtil.custom("参数不合法");
        }
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
