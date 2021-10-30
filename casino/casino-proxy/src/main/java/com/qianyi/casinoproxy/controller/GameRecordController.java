package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.GameRecordVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/gameRecord")
@Api(tags = "报表中心")
public class GameRecordController {
    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private UserService userService;
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
            @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
            @ApiImplicitParam(name = "betId", value = "注单号", required = false),
            @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
            @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
    })
    public ResponseEntity<GameRecordVo> findGameRecordPage(Integer pageSize, Integer pageCode, String user, String betId,
                                                         String gname,Integer gid,String account){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        GameRecord game = new GameRecord();
        if (CasinoProxyUtil.setParameter(game)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        game.setBetId(betId);
        game.setGname(gname);
        game.setGid(gid);
        game.setUser(user);
        if (!CasinoProxyUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (CasinoProxyUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            game.setUserId( byAccount.getId());
        }
        Page<GameRecord> gameRecordPage = gameRecordService.findGameRecordPage(game, pageable);
        PageResultVO<GameRecordVo> pageResultVO =new PageResultVO(gameRecordPage);
        List<GameRecord> content = gameRecordPage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordVo> gameRecordVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(GameRecord::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(gameRecord ->{
                    GameRecordVo gameRecordVo = new GameRecordVo(gameRecord);
                    userList.stream().forEach(u->{
                        if (u.getId().equals(gameRecord.getUserId())){
                            gameRecordVo.setAccount(u.getAccount());
                        }
                    });
                    gameRecordVoList.add(gameRecordVo);
                });
            }
            pageResultVO.setContent(gameRecordVoList);
        }
        return ResponseUtil.success(pageResultVO);
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
        GameRecord game = new GameRecord();
        game.setUser(user);
        game.setBetId(betId);
        game.setGname(gname);
        game.setGid(gid);
        if (CasinoProxyUtil.setParameter(game)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        return ResponseUtil.success(gameRecordService.findRecordRecordSum(game));
    }

}
