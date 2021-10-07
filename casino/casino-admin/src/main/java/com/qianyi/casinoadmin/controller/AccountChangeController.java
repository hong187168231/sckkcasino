package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.AccountChangeVo;
import com.qianyi.casinoadmin.vo.PageResultVO;
import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.AccountChangeService;
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

@Api(tags = "报表中心")
@RestController
@RequestMapping("accountChange")
public class AccountChangeController {
    @Autowired
    private AccountChangeService accountChangeService;

    @Autowired
    private UserService userService;
    /**
     * 分页查询用户账变
     *
     * @param orderNo 订单号
     * @param type 账变类型
     * @param account 会员账号
     * @return
     */
    @ApiOperation("分页查询用户账变")
    @GetMapping("/findAccountChangePage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
            @ApiImplicitParam(name = "type", value = "账变类型", required = false),
            @ApiImplicitParam(name = "account", value = "会员账号", required = false),
    })
    public ResponseEntity<AccountChangeVo> findAccountChangePage(Integer pageSize, Integer pageCode, Integer type, String account, String orderNo){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        AccountChange accountChange = new AccountChange();
        if (!LoginUtil.checkNull(account)){
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)){
                return ResponseUtil.custom("用户不存在");
            }
            accountChange.setUserId(user.getId());
        }
        accountChange.setOrderNo(orderNo);
        accountChange.setType(type);
        Page<AccountChange> accountChangePage = accountChangeService.findAccountChangePage(pageable, accountChange);
        PageResultVO<AccountChangeVo> pageResultVO = new PageResultVO(accountChangePage);
        List<AccountChange> content = accountChangePage.getContent();
        if(content != null && content.size() > 0){
            List<AccountChangeVo> accountChangeVoList =new LinkedList<>();
            List<Long> userIds = content.stream().map(AccountChange::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(change ->{
                    AccountChangeVo accountChangeVo = new AccountChangeVo(change);
                    userList.stream().forEach(user->{
                        if (user.getId().equals(change.getUserId())){
                            accountChangeVo.setAccount(user.getAccount());
                        }
                    });
                    accountChangeVoList.add(accountChangeVo);
                });
            }
            pageResultVO.setContent(accountChangeVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }
}
