package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.service.WithdrawOrderService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("提现列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "订单状态：全部：不传值，0: 正在出款 1：成功出款，2：退回出款，3：已经锁定", required = false),
    })
    @GetMapping("/withdrawList")
    public ResponseEntity withdrawList(Integer pageSize, Integer pageCode, Integer status) {
        Long userId = CasinoWebUtil.getAuthId();
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        withdrawOrder.setUserId(userId);
        if (status != null) {
            withdrawOrder.setStatus(status);
        }
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoWebUtil.setPageable(pageCode, pageSize, sort);
        Page<WithdrawOrder> withdrawOrderPage = withdrawOrderService.findUserPage(pageable, withdrawOrder);
        return ResponseUtil.success(withdrawOrderPage);
    }
}
