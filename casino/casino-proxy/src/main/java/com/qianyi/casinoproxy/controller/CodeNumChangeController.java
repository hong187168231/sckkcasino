package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.CodeNumChange;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.CodeNumChangeService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.CodeNumChangeVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
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
@RequestMapping("codeNumChange")
@Api(tags = "客户中心")
public class CodeNumChangeController {
    @Autowired
    private CodeNumChangeService codeNumChangeService;
    @Autowired
    private UserService userService;
    @ApiOperation("查询用户打码明细表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "id", value = "客户id", required = false),
    })
    @GetMapping("findCodeNumChangeList")
    public ResponseEntity<CodeNumChangeVo> findCodeNumChangeList(Integer pageSize, Integer pageCode,Long id){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        CodeNumChange codeNumChange = new CodeNumChange();
        codeNumChange.setUserId(id);
        Page<CodeNumChange> codeNumChangePage = codeNumChangeService.findCodeNumChangePage(pageable, codeNumChange);
        PageResultVO<CodeNumChangeVo> pageResultVO = new PageResultVO(codeNumChangePage);
        List<CodeNumChange> content = codeNumChangePage.getContent();
        if(content != null && content.size() > 0){
            List<CodeNumChangeVo> accountChangeVoList =new LinkedList<>();
            List<Long> userIds = content.stream().map(CodeNumChange::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(change ->{
                    CodeNumChangeVo accountChangeVo = new CodeNumChangeVo(change);
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
