package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinoadmin.vo.RechargeTurnoverVo;
import com.qianyi.casinocore.model.RechargeTurnover;
import com.qianyi.casinocore.model.User;
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

import java.util.LinkedList;
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
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "account", value = "会员账号", required = false)
    })
    @GetMapping("/findPage")
    public ResponseEntity<RechargeTurnover> findPage(Integer pageSize,Integer pageCode,String account){
        RechargeTurnover rechargeTurnover = new RechargeTurnover();
        if (!LoginUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            rechargeTurnover.setUserId(user.getId());
        }
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<RechargeTurnover> rechargeTurnoverPage = rechargeTurnoverService.findUserPage(pageable, rechargeTurnover);
        PageResultVO<RechargeTurnoverVo> pageResultVO = new PageResultVO(rechargeTurnoverPage);
        List<RechargeTurnover> content = rechargeTurnoverPage.getContent();
        if(content != null && content.size() > 0){
            List<RechargeTurnoverVo> rechargeTurnoverVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(RechargeTurnover::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(recharge->{
                    RechargeTurnoverVo rechargeTurnoverVo = new RechargeTurnoverVo(recharge);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(recharge.getUserId())){
                            rechargeTurnoverVo.setAccount(user.getAccount());
                        }
                    });
                    rechargeTurnoverVoList.add(rechargeTurnoverVo);
                });
            }
            pageResultVO.setContent(rechargeTurnoverVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }

}
