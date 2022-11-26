package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserLevelRecord;
import com.qianyi.casinocore.service.UserLevelService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.LevelChangeBackVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.MessageUtil;
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

@Api(tags = "等级中心")
@RestController
@RequestMapping("levelChange")
public class LevelChangeController {
    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageUtil messageUtil;

    /**
     * 分页查询用户账变
     *
     * @param account 会员账号
     * @return
     */
    @ApiOperation("分页查询用户等级变更记录")
    @GetMapping("/findLevelChangePage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "account", value = "会员账号", required = false),

    })
    public ResponseEntity<LevelChangeBackVo> findAccountChangePage(Integer pageSize, Integer pageCode, String account) {
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        UserLevelRecord userLevel = new UserLevelRecord();
        if (!LoginUtil.checkNull(account)) {
            User user = userService.findByAccount(account);
            if (LoginUtil.checkNull(user)) {
                return ResponseUtil.custom("用户不存在");
            }
            userLevel.setUserId(user.getId());
        }
        Page<UserLevelRecord> levelChangePage = userLevelService.findLevelChangePage(pageable, userLevel);
        PageResultVO<LevelChangeBackVo> pageResultVO = new PageResultVO(levelChangePage);
        List<UserLevelRecord> content = levelChangePage.getContent();
        if (content != null && content.size() > 0) {
            List<LevelChangeBackVo> accountChangeVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(UserLevelRecord::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if (userList != null) {
                content.stream().forEach(change -> {
                    LevelChangeBackVo accountChangeVo = new LevelChangeBackVo(change);
                    userList.stream().forEach(user -> {
                        if (user.getId().equals(change.getUserId())) {
                            accountChangeVo.setAccount(user.getAccount());
                        }
                    });
                    userLevelService.processLevelWater2(change);
                    accountChangeVoList.add(accountChangeVo);
                });
            }
            pageResultVO.setContent(accountChangeVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }
}
