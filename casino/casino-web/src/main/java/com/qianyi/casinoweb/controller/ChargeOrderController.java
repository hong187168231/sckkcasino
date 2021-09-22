package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.service.ChargeOrderService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * 充值申请
 */
@Slf4j
@RestController
@RequestMapping("/chargeOrder")
@Api(tags = "资金中心")
public class ChargeOrderController {
    @Autowired
    private ChargeOrderService chargeOrderService;
    /**
     * 用户充值列表
     *
     * @param status 状态:1.入款审核中,2.入款成功，3.入款已取消
     * @return
     */
    @ApiOperation("用户充值列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "状态:全部.不传值，1.入款审核中,2.入款成功，3.入款已取消", required = false),
    })
    @GetMapping("/chargeOrderList")
    public ResponseEntity chargeOrderList(Integer pageSize, Integer pageCode, Integer status){
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoWebUtil.setPageable(pageCode, pageSize, sort);
        Specification<ChargeOrder> condition = this.getCondition(status,userId);
        Page<ChargeOrder> chargeOrderPage = chargeOrderService.findChargeOrderPage(condition, pageable);
        return ResponseUtil.success(chargeOrderPage);
    }

    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<ChargeOrder> getCondition(Integer status, Long userId) {
        Specification<ChargeOrder> specification = new Specification<ChargeOrder>() {
            @Override
            public Predicate toPredicate(Root<ChargeOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (status !=null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), status));
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
