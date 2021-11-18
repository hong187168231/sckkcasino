package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.vo.AccountChangeBackVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.AccountChangeService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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
     * @param types 账变类型
     * @param account 会员账号
     * @return
     */
    @ApiOperation("分页查询用户账变")
    @PostMapping(value = "/findAccountChangePage")
    public ResponseEntity<AccountChangeBackVo> findAccountChangePage(@RequestParam(value = "每页大小(默认10条)", required = false) Integer pageSize,
                                                                     @RequestParam(value = "当前页(默认第一页)", required = false) Integer pageCode,
                                                                     @RequestBody (required = false) Integer[] types,
                                                                     @RequestParam(value = "会员账号", required = false) String account,
                                                                     @RequestParam(value = "订单号", required = false) String orderNo,
                                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
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
        Page<AccountChange> accountChangePage = accountChangeService.findAccountChangePage(pageable,types, accountChange,startDate,endDate);
        PageResultVO<AccountChangeBackVo> pageResultVO = new PageResultVO(accountChangePage);
        List<AccountChange> content = accountChangePage.getContent();
        if(content != null && content.size() > 0){
            List<AccountChangeBackVo> accountChangeVoList =new LinkedList<>();
            List<Long> userIds = content.stream().map(AccountChange::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(change ->{
                    AccountChangeBackVo accountChangeVo = new AccountChangeBackVo(change);
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

    @ApiOperation("查询账变类型")
    @GetMapping("/getData")
    public ResponseEntity getData(){
        AccountChangeEnum[] values = AccountChangeEnum.values();
        Map<Integer,String> map = new HashMap<>();
        for (AccountChangeEnum accountChangeEnum:values){
            map.put(accountChangeEnum.getType(),accountChangeEnum.getName());
        }
        return ResponseUtil.success(map);
    }
}
