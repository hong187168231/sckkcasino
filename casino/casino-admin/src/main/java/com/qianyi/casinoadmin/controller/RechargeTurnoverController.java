package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.RechargeTurnover;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.service.RechargeTurnoverService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/rechargeTurnover")
@Api(tags = "资金中心")
public class RechargeTurnoverController {

    @Autowired
    private RechargeTurnoverService rechargeTurnoverService;

    @Autowired
    private UserService userService;

    @ApiOperation("充值订单流水记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false)
    })
    @GetMapping("/findPage")
    public ResponseEntity<RechargeTurnover> findPage(Integer pageSize,Integer pageCode){
        RechargeTurnover rechargeTurnover = new RechargeTurnover();
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<RechargeTurnover> rechargeTurnoverPage = rechargeTurnoverService.findUserPage(pageable, rechargeTurnover);
        List<RechargeTurnover> content = rechargeTurnoverPage.getContent();
        if(content != null && content.size() > 0){
            List<Long> userIds = content.stream().map(RechargeTurnover::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null && userList.size() > 0){
                userList.stream().forEach(user ->{
                    content.stream().forEach(recharge->{
                        if (user.getId().equals(recharge.getUserId())){
                            recharge.setAccount(user.getAccount());
                        }
                    });
                });
            }
        }
        return ResponseUtil.success(rechargeTurnoverPage);
    }

}
