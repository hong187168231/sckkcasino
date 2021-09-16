package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.Order;
import com.qianyi.casinocore.service.OrderService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("user")
@Api(tags = "资金中心")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * 查询操作
     * 注意：jpa 是从第0页开始的
     * @return
     */
    @ApiOperation("订单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "userId", value = "用户Id", required = false),
            @ApiImplicitParam(name = "no", value = "订单号", required = false),
    })
    @GetMapping("findOrderList")
    public ResponseEntity findOrderList(Integer pageSize, Integer pageCode, Long userId, String no){
        Order order = new Order();
        order.setUserId(userId);
        order.setNo(no);
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<Order> userPage = orderService.findOrderPage(pageable, order);
        return ResponseUtil.success(userPage);
    }

    @ApiOperation("添加订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户名", required = true),
            @ApiImplicitParam(name = "money", value = "订单金额", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", required = false),
    })
    @PostMapping("saveOrder")
    public ResponseEntity saveOrder(Long userId, BigDecimal money, String remark){
        Order order = new Order();
        order.setUserId(userId);
        order.setMoney(money);
        order.setNo(orderService.getOrderNo());
        order.setRemark(remark + "");
        order.setState(Constants.order_wait);
        orderService.save(order);
        return ResponseUtil.success();
    }

    @ApiOperation("修改订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "no", value = "订单号", required = true),
            @ApiImplicitParam(name = "status", value = "订单状态", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", required = false),
    })
    @PostMapping("updateOrder")
    public ResponseEntity updateOrder(String no, Integer status, String remark){
        Order order = orderService.findByNo(no);
        if(order == null){
            return ResponseUtil.custom("订单不存在");
        }

        order.setState(status);
        if(LoginUtil.checkNull(remark)){
            order.setRemark(remark);
        }

        orderService.save(order);
        return ResponseUtil.success();
    }



}
