package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.business.ChargeBusiness;
import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ChargeOrderService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 充值申请
 */
@Slf4j
@RestController
@RequestMapping("/chargeOrder")
@Api(tags = "资金管理")
public class ChargeOrderController {
    @Autowired
    private ChargeOrderService chargeOrderService;
    @Autowired
    private ChargeBusiness chargeBusiness;
    /**
     * 充值申请列表
     *
     * @param status 状态(0未确认 1已确认)
     * @param orderNo 充值订单号
     * @param userId 会员id userId
     * @return
     */
    @ApiOperation("充值申请列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "状态(0未确认 1已确认)", required = false),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
            @ApiImplicitParam(name = "userId", value = "会员ID", required = false),
    })
    @GetMapping("/chargeOrderList")
    public ResponseEntity chargeOrderList(Integer pageSize, Integer pageCode, Integer status, String orderNo, Long userId){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Specification<ChargeOrder> condition = this.getCondition(status,orderNo,userId);
        Page<ChargeOrder> chargeOrderPage = chargeOrderService.findChargeOrderPage(condition, pageable);
        return ResponseUtil.success(chargeOrderPage);
    }
    /**
     * 充值申请列表
     *
     * @param id 充值订单id
     * @param status 充值订单审核状态 0：未确认，1：已确认
     * @return
     */
    @ApiOperation("后台充值上分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", required = true),
            @ApiImplicitParam(name = "status", value = "审核状态，0：未确认，1：已确认", required = true),
    })
    @PostMapping("/saveChargeOrder")
    public ResponseEntity saveChargeOrder(Long id, Integer status){
        if(status != CommonConst.NUMBER_1){
            return ResponseUtil.custom("参数不合法");
        }
        ChargeOrder chargeOrder = chargeOrderService.findChargeOrderByIdUseLock(id);
        if(chargeOrder == null||chargeOrder.getStatus() != CommonConst.NUMBER_0){
            return ResponseUtil.custom("充值订单已失效");
        }
        chargeOrder.setStatus(status);
        return chargeBusiness.updateChargeOrderAndUser(chargeOrder);
    }

    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<ChargeOrder> getCondition(Integer status, String orderNo, Long userId) {
        Specification<ChargeOrder> specification = new Specification<ChargeOrder>() {
            @Override
            public Predicate toPredicate(Root<ChargeOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (status !=null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), status));
                }
                if (!CommonUtil.checkNull(orderNo)) {
                    list.add(cb.equal(root.get("orderNo").as(String.class), orderNo));
                }
                if (userId != null ) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userId));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));

                return predicate;
            }
        };
        return specification;
    }
}
