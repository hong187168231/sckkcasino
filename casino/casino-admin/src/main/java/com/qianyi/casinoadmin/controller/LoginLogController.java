package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.service.LoginLogService;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/loginLog")
@Api(tags = "用户管理")
public class LoginLogController {
    @Autowired
    private LoginLogService loginLogService;
    @ApiOperation("分页查询用户登录日志")
    @GetMapping("/findLoginLogPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "ip", value = "ip", required = false),
            @ApiImplicitParam(name = "userId", value = "用户id", required = false),
            @ApiImplicitParam(name = "account", value = "用户账号", required = false),
            @ApiImplicitParam(name = "startTime", value = "搜素起始时间", required = false),
            @ApiImplicitParam(name = "endTime", value = "搜素结束时间", required = false),
    })
    public ResponseEntity findGameReportPage(Integer pageSize, Integer pageCode,String ip,Long userId,String account,
                                             Date startTime,Date endTime){
        Sort sort = Sort.by("createTime").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Specification<LoginLog> condition = this.getCondition(ip,userId,account,startTime,endTime);
        Page<LoginLog> loginLogPage = loginLogService.findLoginLogPage(condition, pageable);
        return ResponseUtil.success(loginLogPage);

    }

    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<LoginLog> getCondition(String ip,Long userId,String account,
                                                 Date startTime,Date endTime) {
        Specification<LoginLog> specification = new Specification<LoginLog>() {
            @Override
            public Predicate toPredicate(Root<LoginLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (ip !=null && ip != "") {
                    list.add(cb.equal(root.get("ip").as(String.class), ip));
                }
                if (userId !=null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userId));
                }
                if (account != null && account != "") {
                    list.add(cb.equal(root.get("account").as(String.class), account));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                if (startTime != null) {
                    predicate.getExpressions().add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startTime));
                }
                if (endTime != null) {
                    predicate.getExpressions().add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), endTime));
                }
                return predicate;
            }
        };
        return specification;
    }
}
