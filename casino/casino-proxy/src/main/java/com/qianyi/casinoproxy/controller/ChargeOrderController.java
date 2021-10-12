package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.business.ChargeOrderBusiness;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ChargeOrderService;
import com.qianyi.casinocore.service.CollectionBankcardService;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.ChargeOrderVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private ChargeOrderBusiness chargeOrderBusiness;

    @Autowired
    private CollectionBankcardService collectionBankcardService;

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserService sysUserService;
    /**
     * 充值申请列表
     *
     * @param status 状态(0未确认 1已确认)
     * @param orderNo 充值订单号
     * @param account 会员账号 account
     * @return
     */
    @ApiOperation("充值申请列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "状态(0未确认 1已确认)", required = false),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
            @ApiImplicitParam(name = "account", value = "会员账号", required = false),
    })
    @GetMapping("/chargeOrderList")
    public ResponseEntity<ChargeOrderVo> chargeOrderList(Integer pageSize, Integer pageCode, Integer status, String orderNo, String account){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        Long userId = null;
        if (!CasinoProxyUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (CasinoProxyUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = user.getId();
        }
        Specification<ChargeOrder> condition = this.getCondition(status,orderNo,userId);
        Page<ChargeOrder> chargeOrderPage = chargeOrderService.findChargeOrderPage(condition, pageable);
        PageResultVO<ChargeOrderVo> pageResultVO =new PageResultVO(chargeOrderPage);
        List<ChargeOrder> content = chargeOrderPage.getContent();
        if(content != null && content.size() > 0){
            List<ChargeOrderVo> chargeOrderVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(ChargeOrder::getUserId).collect(Collectors.toList());
            List<Long> collect = content.stream().map(ChargeOrder::getBankcardId).collect(Collectors.toList());
            List<String> updateBys = content.stream().map(ChargeOrder::getUpdateBy).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            List<CollectionBankcard> all = collectionBankcardService.findAll(collect);
            List<SysUser> sysUsers = sysUserService.findAll(updateBys);
            Map<Long, CollectionBankcard> bankcardMap = all.stream().collect(Collectors.toMap(CollectionBankcard::getId, a -> a, (k1, k2) -> k1));
            if(userList != null){
                content.stream().forEach(chargeOrder ->{
                    ChargeOrderVo chargeOrderVo = new ChargeOrderVo(chargeOrder);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(chargeOrder.getUserId())){
                            chargeOrderVo.setAccount(user.getAccount());
                            this.setCollectionBankcard(bankcardMap.get(chargeOrder.getBankcardId()),chargeOrderVo);
                        }
                    });
                    sysUsers.stream().forEach(sysUser->{
                        if (chargeOrder.getStatus() != CommonConst.NUMBER_0 && sysUser.getId().toString().equals(chargeOrder.getUpdateBy() == null?"":chargeOrder.getUpdateBy())){
                            chargeOrderVo.setUpdateBy(sysUser.getUserName());
                        }
                    });
                    chargeOrderVoList.add(chargeOrderVo);
                });
            }
            pageResultVO.setContent(chargeOrderVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }
    private void  setCollectionBankcard(CollectionBankcard collectionBankcard,ChargeOrderVo chargeOrderVo){
        if (CasinoProxyUtil.checkNull(collectionBankcard)){
            return ;
        }
        chargeOrderVo.setBankNo(collectionBankcard.getBankNo());
        chargeOrderVo.setAccountName(collectionBankcard.getAccountName());
    }

    /**
     * 充值申请列表
     *
     * @param id 充值订单id
     * @param status 汇款状态，0.未确认。 1.成功   2.失败, 3.失效
     * @return
     */
    @ApiOperation("审核充值订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", required = true),
            @ApiImplicitParam(name = "status", value = "汇款状态， 1.成功  2.失败", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", required = false),
//            @ApiImplicitParam(name = "money", value = "上分金额", required = false),
    })
    @PostMapping("/updateChargeOrder")
    public ResponseEntity updateChargeOrder(Long id, Integer status,String remark){
        if (CasinoProxyUtil.checkNull(id,status)){
            ResponseUtil.custom("参数不合法");
        }
        if(status != CommonConst.NUMBER_1 && status != CommonConst.NUMBER_2){
            return ResponseUtil.custom("参数不合法");
        }

        return chargeOrderBusiness.checkOrderSuccess(id,status,remark);
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
