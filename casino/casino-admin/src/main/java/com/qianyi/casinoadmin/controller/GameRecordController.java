package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
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
@RequestMapping("/gameRecord")
@Api(tags = "报表中心")
public class GameRecordController {
    @Autowired
    private GameRecordService gameRecordService;
    /**
     * 分页查询三方游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gname 游戏名称
     * @param gid 游戏类型
     * @return
     */
    @ApiOperation("分页查询三方游戏注单")
    @GetMapping("/findGameRecordPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "user", value = "会员账号", required = false),
            @ApiImplicitParam(name = "betId", value = "注单号", required = false),
            @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
            @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
    })
    public ResponseEntity<GameRecord> findGameRecordPage(Integer pageSize, Integer pageCode, String user, String betId,String gname,Integer gid){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Specification<GameRecord> condition = this.getCondition(user,betId,gname,gid);
        Page<GameRecord> gameRecordPage = gameRecordService.findGameRecordPage(condition, pageable);
        return ResponseUtil.success(gameRecordPage);
    }
    /**
     * 统计三方游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gname 游戏名称
     * @param gid 游戏类型
     * @return
     */
    @ApiOperation("统计三方游戏注单")
    @GetMapping("/findRecordRecordSum")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "会员账号", required = false),
            @ApiImplicitParam(name = "betId", value = "注单号", required = false),
            @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
            @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
    })
    public ResponseEntity findRecordRecordSum(String user,String betId,String gname,Integer gid){
        return ResponseUtil.success(gameRecordService.findRecordRecordSum(user,betId,gname,gid));
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<GameRecord> getCondition(String user,String betId,String gname,Integer gid) {
        Specification<GameRecord> specification = new Specification<GameRecord>() {
            @Override
            public Predicate toPredicate(Root<GameRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(user)) {
                    list.add(cb.equal(root.get("user").as(String.class), user));
                }
                if (!CommonUtil.checkNull(betId)) {
                    list.add(cb.equal(root.get("betId").as(String.class), betId));
                }
                if (!CommonUtil.checkNull(gname)) {
                    list.add(cb.equal(root.get("gname").as(String.class), gname));
                }
                if (gid != null) {
                    list.add(cb.equal(root.get("gid").as(Integer.class), gid));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
}
