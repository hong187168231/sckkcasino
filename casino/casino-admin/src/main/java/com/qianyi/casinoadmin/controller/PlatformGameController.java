package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.AdGame;
import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.PlatformGame;
import com.qianyi.casinocore.service.AdGamesService;
import com.qianyi.casinocore.service.PlatformGameService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏管理
 */
@Slf4j
@RestController
@RequestMapping("/adGame")
@Api(tags = "游戏中心")
public class PlatformGameController {

    @Autowired
    private AdGamesService adGameService;

    @Autowired
    private PlatformGameService platformGameService;

    @GetMapping("/platformList")
    @ApiOperation("平台列表")
    public ResponseEntity<PlatformGame> platformList() {
        List<PlatformGame> platformGames =  platformGameService.findAll();
        return ResponseUtil.success(platformGames);
    }

    @ApiOperation("三方平台维护")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "gameStatus", value = "平台状态：0：维护，1：正常 2：下架", required = true)
    })
    @PostMapping("/updatePlatformStatus")
    public ResponseEntity<PlatformGame> updatePlatformStatus(Long id,Integer gameStatus){
        if(gameStatus != CommonConst.NUMBER_0 && gameStatus != CommonConst.NUMBER_1 && gameStatus != CommonConst.NUMBER_2)
            return ResponseUtil.success("参数不合法");
        PlatformGame platformGame = platformGameService.findById(id);
        if(platformGame == null){
            return ResponseUtil.success("数据不存在");
        }
        platformGame.setGameStatus(gameStatus);
        platformGameService.save(platformGame);
        return ResponseUtil.success();
    }

    @GetMapping("/findGameList")
    @ApiOperation("游戏列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "gamePlatformName", value = "游戏名称", required = true),
            @ApiImplicitParam(name = "gamesStatus", value = "是否维护 0：维护 1：正常, 2：下架", required = false),
            @ApiImplicitParam(name = "platformId", value = "1:PG 2:CQ9", required = false)
    })
    public ResponseEntity<AdGame> findGameList(Integer pageSize, Integer pageCode,String gamePlatformName, Integer platformId) {
        if (LoginUtil.checkNull(gamePlatformName)) {
            return ResponseUtil.success("数据不存在");
        }
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize);
        AdGame adGame = new AdGame();
        if(!LoginUtil.checkNull(gamePlatformName)){
            adGame.setGameName(gamePlatformName);
        }
        if(platformId != null){
            adGame.setGamePlatformId(platformId);
        }

        Page<AdGame> adGamePage =  adGameService.findAll(pageable, adGame);
        return ResponseUtil.success(adGamePage);
    }

    @ApiOperation("游戏维护")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameIdList", value = "id集合", required = true),
            @ApiImplicitParam(name = "gameStatus", value = "平台状态：0：维护，1：正常, 2:下架", required = true)
    })
    @PostMapping("/updateDomainName")
    public ResponseEntity<AdGame> updateGameStatus(@RequestBody List<String> gameIdList, Integer gameStatus){
        if(gameStatus != CommonConst.NUMBER_0 && gameStatus != CommonConst.NUMBER_1 && gameStatus != CommonConst.NUMBER_2)
            return ResponseUtil.success("参数不合法");

        if(gameIdList == null || gameIdList.isEmpty()){
            return ResponseUtil.success("参数不合法");
        }
        Specification<AdGame> condition = this.getCondition(gameIdList);
        List<AdGame> adGameList = adGameService.findAllConnection(condition);
        if(adGameList == null || adGameList.isEmpty()){
            return ResponseUtil.success("没有数据");
        }
        adGameList.stream().forEach(adGame -> adGame.setGamesStatus(gameStatus));
        adGameService.saveAll(adGameList);
        return ResponseUtil.success(adGameList);
    }

    private Specification<AdGame> getCondition(List<String> gameIdList) {
        Specification<AdGame> specification = new Specification<AdGame>() {
            @Override
            public Predicate toPredicate(Root<AdGame> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (gameIdList != null && gameIdList.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (String id : gameIdList) {
                        try {
                            in.value(Long.valueOf(id));
                        }catch (Exception e){
                            continue;
                        }
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}
