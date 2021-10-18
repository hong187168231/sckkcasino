package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.WithdrawOrderVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private UserService userService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private ProxyUserService proxyUserService;

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
    public ResponseEntity<WithdrawOrderVo> withdrawList(Integer pageSize,Integer pageCode, Integer status, String account, String no, String bankId){
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        if (CasinoProxyUtil.setParameter(withdrawOrder)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        withdrawOrder.setStatus(status);
        withdrawOrder.setNo(no);
        withdrawOrder.setBankId(bankId);
        if (!CasinoProxyUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (CasinoProxyUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            withdrawOrder.setUserId(user.getId());
        }
        Sort sort=Sort.by("id").descending();
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        Page<WithdrawOrder> withdrawOrderPage = withdrawOrderService.findUserPage(pageable, withdrawOrder);
        PageResultVO<WithdrawOrderVo> pageResultVO = new PageResultVO(withdrawOrderPage);
        List<WithdrawOrder> content = withdrawOrderPage.getContent();
        if(content != null && content.size() > 0){
            List<WithdrawOrderVo> withdrawOrderVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(WithdrawOrder::getUserId).collect(Collectors.toList());
            List<String> collect = content.stream().map(WithdrawOrder::getBankId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            List<Bankcards> all = bankcardsService.findAll(collect);
//            List<String> updateBys = content.stream().map(WithdrawOrder::getUpdateBy).collect(Collectors.toList());
//            List<ProxyUser> proxyUsers = proxyUserService.findProxyUsers(updateBys);
            Map<Long, Bankcards> bankcardMap = all.stream().collect(Collectors.toMap(Bankcards::getId, a -> a, (k1, k2) -> k1));
            if(userList != null){
                content.stream().forEach(withdraw ->{
                    WithdrawOrderVo withdrawOrderVo = new WithdrawOrderVo(withdraw);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(withdraw.getUserId())){
                            withdrawOrderVo.setAccount(user.getAccount());
                            try {
                                this.setBankcards(bankcardMap.get(Long.valueOf(withdrawOrderVo.getBankId())),withdrawOrderVo);
                            }catch (Exception ex){

                            }
                        }
                    });
//                    proxyUsers.stream().forEach(proxyUser->{
//                        if (withdraw.getStatus() != CommonConst.NUMBER_0 && proxyUser.getId().toString().equals(withdraw.getUpdateBy() == null?"":withdraw.getUpdateBy())){
//                            withdrawOrderVo.setUpdateBy(proxyUser.getUserName());
//                        }
//                    });
                    withdrawOrderVoList.add(withdrawOrderVo);
                });
            }
            pageResultVO.setContent(withdrawOrderVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }
    private void  setBankcards(Bankcards bankcards,WithdrawOrderVo withdrawOrderVo){
        if (CasinoProxyUtil.checkNull(bankcards)){
            return ;
        }
        withdrawOrderVo.setBankNo(bankcards.getBankAccount());
        withdrawOrderVo.setAccountName(bankcards.getRealName());
    }
    @ApiOperation("提现审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", required = true),
            @ApiImplicitParam(name = "status", value = "审核状态，1：通过，2：拒绝", required = true),
    })
    @PostMapping("saveWithdraw")
    public ResponseEntity saveWithdraw(Long id, Integer status){
        if (CasinoProxyUtil.checkNull(id,status)){
            return ResponseUtil.custom("参数不合法");
        }
        if(status != CommonConst.NUMBER_1 && status != CommonConst.NUMBER_2){
            return ResponseUtil.custom("参数不合法");
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        String lastModifier = (byId == null || byId.getUserName() == null)? "" : byId.getUserName();
        return withdrawBusiness.updateWithdrawAndUser(id,status,lastModifier);
    }
}
