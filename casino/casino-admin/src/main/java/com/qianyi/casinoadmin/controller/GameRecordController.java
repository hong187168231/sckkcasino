package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TranslateConst;
import com.qianyi.casinocore.vo.*;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("gameRecord")
@Api(tags = "报表中心")
public class GameRecordController {

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordObdjService gameRecordObdjService;

    @Autowired
    private GameRecordObtyService gameRecordObtyService;

    @Autowired
    private GameRecordObzrService  gameRecordObzrService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private GameRecordAeService gameRecordAeService;

    @Autowired
    private UserService userService;

//    @Autowired
//    private GameRecordVNCService gameRecordVNCService;
    @Autowired
    private GameRecordDMCService gameRecordDMCService;

    @Autowired
    private GameRecordDGService gameRecordDGService;

    @Autowired
    private RptBetInfoDetailService rptBetInfoDetailService;

    /**
     * 分页查询WM游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gname 游戏名称
     * @param gid 游戏类型
     * @return
     */
    @ApiOperation("分页查询WM游戏注单")
    @GetMapping("/findGameRecordPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
        @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    public ResponseEntity<GameRecordVo> findGameRecordPage(Integer pageSize, Integer pageCode, String user, String betId,
        String gname,Integer gid,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecord game = new GameRecord();
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        game.setUserId(userId);
        game.setBetId(betId);
        game.setGname(gname);
        game.setGid(gid);
        game.setUser(user);
        Page<GameRecord> gameRecordPage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                gameRecordPage = gameRecordService.findGameRecordPage(game, pageable,startTime,endTime,null,null);
            }else {
                gameRecordPage = gameRecordService.findGameRecordPage(game, pageable,null,null,startTime,endTime);
            }
        }else {
            gameRecordPage = gameRecordService.findGameRecordPage(game, pageable,null,null,null,null);
        }
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
     *findRecordRecordSum
     * @param user 会员账号
     * @param betId 注单号
     * @param gname 游戏名称
     * @param gid 游戏类型
     * @return
     */
    @ApiOperation("统计三方游戏注单")
    @GetMapping("/findGameRecordTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
        @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordTotalVo> findRecordRecordSum(String user,String betId,String gname,Integer gid,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecord game = new GameRecord();
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            game.setUserId(byAccount.getId());
        }
        game.setUser(user);
        game.setBetId(betId);
        game.setGname(gname);
        game.setGid(gid);
        GameRecord recordRecordSum;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);

            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                recordRecordSum = gameRecordService.findRecordRecordSum(game, startTime, endTime, null, null);
            }else {
                recordRecordSum = gameRecordService.findRecordRecordSum(game,null,null,startTime,endTime);
            }
        }else {
            recordRecordSum = gameRecordService.findRecordRecordSum(game,null,null,null,null);
        }
        GameRecordTotalVo gameRecordTotalVo = new GameRecordTotalVo();
        if (!LoginUtil.checkNull(recordRecordSum)){
            if (StringUtils.hasText(recordRecordSum.getBet())){
                gameRecordTotalVo.setBet(new BigDecimal(recordRecordSum.getBet()));
            }
            if (StringUtils.hasText(recordRecordSum.getValidbet())){
                gameRecordTotalVo.setValidbet(new BigDecimal(recordRecordSum.getValidbet()));
            }
            if (StringUtils.hasText(recordRecordSum.getBet())){
                gameRecordTotalVo.setWinLoss(new BigDecimal(recordRecordSum.getWinLoss()));
            }
        }
        return ResponseUtil.success(gameRecordTotalVo);
    }
    /**
     * 分页查询PG游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gameCode 游戏代码
     * @return
     */
    @ApiOperation("分页查询PG游戏注单")
    @GetMapping("/findPgGameRecordPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "gameCode", value = "游戏代码", required = false),
        @ApiImplicitParam(name = "parentBetId", value = "父主单号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "transType", value = "Stake:下注 Payoff:派彩", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询结算起始时间", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结算结束时间", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordGoldenFVo> findPgGameRecordPage(Integer pageSize, Integer pageCode,
        String user, String betId, String gameCode,String parentBetId,String account,String transType,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordGoldenF gameRecordGoldenF = new GameRecordGoldenF();
        gameRecordGoldenF.setVendorCode(Constants.PLATFORM_PG);
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        return selectPGOrCQ9(gameRecordGoldenF,pageable,user,betId,gameCode,parentBetId,account,startDate,endDate,transType);
    }

    /**
     * 统计PG游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gameCode 游戏代码
     * @return
     */
    @ApiOperation("统计PG游戏注单")
    @GetMapping("/findPgGameRecordTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "gameCode", value = "游戏代码", required = false),
        @ApiImplicitParam(name = "parentBetId", value = "父主单号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "transType", value = "Stake:下注 Payoff:派彩", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询结算起始时间", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结算结束时间", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordGoldenFTotalVo> findPgGameRecordTotal(String user, String betId, String gameCode,String parentBetId,String account,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,String transType,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordGoldenF gameRecordGoldenF = new GameRecordGoldenF();
        gameRecordGoldenF.setVendorCode(Constants.PLATFORM_PG);
        return selectPGOrCQ9Total(gameRecordGoldenF,user,betId,gameCode,parentBetId,account,startDate,endDate,transType);
    }

    /**
     * 统计CQ9游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gameCode 游戏代码
     * @return
     */
    @ApiOperation("统计CQ9游戏注单")
    @GetMapping("/findCq9GameRecordTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "gameCode", value = "游戏代码", required = false),
        @ApiImplicitParam(name = "parentBetId", value = "父主单号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "transType", value = "Stake:下注 Payoff:派彩", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询结算起始时间", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结算结束时间", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordGoldenFTotalVo> findCq9GameRecordTotal(String user, String betId, String gameCode,String parentBetId,String account,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,String transType,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordGoldenF gameRecordGoldenF = new GameRecordGoldenF();
        gameRecordGoldenF.setVendorCode(Constants.PLATFORM_CQ9);
        return selectPGOrCQ9Total(gameRecordGoldenF,user,betId,gameCode,parentBetId,account,startDate,endDate,transType);
    }
    /**
     * 分页查询CQ9游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gameCode 游戏代码
     * @return
     */
    @ApiOperation("分页查询CQ9游戏注单")
    @GetMapping("/findCq9GameRecordPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "gameCode", value = "游戏代码", required = false),
        @ApiImplicitParam(name = "parentBetId", value = "父主单号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "transType", value = "Stake:下注 Payoff:派彩", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询结算起始时间", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结算结束时间", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordGoldenFVo> findCq9GameRecordPage(Integer pageSize, Integer pageCode,
        String user, String betId, String gameCode,String parentBetId,String account,String transType,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordGoldenF gameRecordGoldenF = new GameRecordGoldenF();
        gameRecordGoldenF.setVendorCode(Constants.PLATFORM_CQ9);
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        return selectPGOrCQ9(gameRecordGoldenF,pageable,user,betId,gameCode,parentBetId,account,startDate,endDate,transType);
    }

    /**
     * 统计SBTY游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gameCode 游戏代码
     * @return
     */
    @ApiOperation("统计SBTY游戏注单")
    @GetMapping("/findSbtyGameRecordTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "gameCode", value = "游戏代码", required = false),
        @ApiImplicitParam(name = "parentBetId", value = "父主单号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "transType", value = "Stake:下注 Payoff:派彩", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询结算起始时间", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结算结束时间", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordGoldenFTotalVo> findSbtyGameRecordTotal(String user, String betId, String gameCode,String parentBetId,String account,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,String transType,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordGoldenF gameRecordGoldenF = new GameRecordGoldenF();
        gameRecordGoldenF.setVendorCode(Constants.PLATFORM_SABASPORT);
        return selectPGOrCQ9Total(gameRecordGoldenF,user,betId,gameCode,parentBetId,account,startDate,endDate,transType);
    }
    /**
     * 分页查询SBTY游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param gameCode 游戏代码
     * @return
     */
    @ApiOperation("分页查询SBTY游戏注单")
    @GetMapping("/findSbtyGameRecordPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "gameCode", value = "游戏代码", required = false),
        @ApiImplicitParam(name = "parentBetId", value = "父主单号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "transType", value = "Stake:下注 Payoff:派彩", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询结算起始时间", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结算结束时间", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordGoldenFVo> findSbtyGameRecordPage(Integer pageSize, Integer pageCode,
        String user, String betId, String gameCode,String parentBetId,String account,String transType,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordGoldenF gameRecordGoldenF = new GameRecordGoldenF();
        gameRecordGoldenF.setVendorCode(Constants.PLATFORM_SABASPORT);
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        return selectPGOrCQ9(gameRecordGoldenF,pageable,user,betId,gameCode,parentBetId,account,startDate,endDate,transType);
    }

    private ResponseEntity<GameRecordGoldenFTotalVo> selectPGOrCQ9Total(GameRecordGoldenF gameRecordGoldenF,String user,
        String betId, String gameCode,String parentBetId,String account,Date startDate,Date endDate,String transType){
        gameRecordGoldenF.setPlayerName(user);
        gameRecordGoldenF.setBetId(betId);
        gameRecordGoldenF.setGameCode(gameCode);
        gameRecordGoldenF.setParentBetId(parentBetId);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordGoldenF.setUserId(userId);

        GameRecordGoldenF goldenF;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            goldenF = gameRecordGoldenFService.findRecordRecordSum(gameRecordGoldenF, startTime, endTime);
        }else {
            goldenF = gameRecordGoldenFService.findRecordRecordSum(gameRecordGoldenF, null, null);
        }
        GameRecordGoldenFTotalVo gameRecordGoldenFTotalVo = new GameRecordGoldenFTotalVo();
        gameRecordGoldenFTotalVo.setBetAmount(goldenF.getBetAmount());
        gameRecordGoldenFTotalVo.setWinAmount(goldenF.getWinAmount());
        gameRecordGoldenFTotalVo.setWinLoss(goldenF.getWinAmount().subtract(goldenF.getBetAmount()));
        return ResponseUtil.success(gameRecordGoldenFTotalVo);
    }

    private ResponseEntity<GameRecordGoldenFVo> selectPGOrCQ9(GameRecordGoldenF gameRecordGoldenF,Pageable pageable,String user,
        String betId, String gameCode,String parentBetId,String account,Date startDate,Date endDate,String transType){
        gameRecordGoldenF.setPlayerName(user);
        gameRecordGoldenF.setBetId(betId);
        gameRecordGoldenF.setGameCode(gameCode);
        gameRecordGoldenF.setParentBetId(parentBetId);
        gameRecordGoldenF.setTransType(transType);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordGoldenF.setUserId(userId);
        Page<GameRecordGoldenF> gameRecordGoldenFPage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            gameRecordGoldenFPage = gameRecordGoldenFService.findGameRecordGoldenFPage(gameRecordGoldenF, pageable, startTime, endTime);
        }else {
            gameRecordGoldenFPage = gameRecordGoldenFService.findGameRecordGoldenFPage(gameRecordGoldenF, pageable, null, null);
        }

        PageResultVO<GameRecordGoldenFVo> pageResultVO =new PageResultVO(gameRecordGoldenFPage);
        List<GameRecordGoldenF> content = gameRecordGoldenFPage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordGoldenFVo> gameRecordVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(GameRecordGoldenF::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(gameRecord ->{
                    GameRecordGoldenFVo vo = new GameRecordGoldenFVo();
                    BeanUtils.copyProperties(gameRecord,vo);
                    vo.setWinLoss(vo.getWinAmount().subtract(vo.getBetAmount()));
                    userList.stream().forEach(u->{
                        if (u.getId().equals(gameRecord.getUserId())){
                            vo.setAccount(u.getAccount());
                        }
                    });
                    gameRecordVoList.add(vo);
                });
            }
            pageResultVO.setContent(gameRecordVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }


    /**
     * 分页查询OBDJ游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param betStatus 注单状态
     * @return
     */
    @ApiOperation("分页查询OBDJ游戏注单")
    @GetMapping("/findOBDJGameRecordPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "betStatus", value = "注单状态3-待结算4-已取消5-赢(已中奖) 6-输(未中奖) 7-已撤销8-赢半9-输半10-走水", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordObdjVo> findOBDJGameRecordPage(Integer pageSize, Integer pageCode, String user, String betId,
        Integer betStatus,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecordObdj gameRecordObdj = new GameRecordObdj();
        gameRecordObdj.setMemberAccount(user);
        gameRecordObdj.setBetStatus(betStatus);
        if (!LoginUtil.checkNull(betId)){

            try {
                Long betIdLong = Long.parseLong(betId);
                gameRecordObdj.setBetId(betIdLong);
            }catch (Exception ex){
                return ResponseUtil.custom("参数不合法");
            }
        }
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordObdj.setUserId(userId);
        Page<GameRecordObdj> gameRecordObdjPage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                gameRecordObdjPage = gameRecordObdjService.findGameRecordPage(gameRecordObdj,pageable,startTime,endTime,null,null);
            }else {
                gameRecordObdjPage = gameRecordObdjService.findGameRecordPage(gameRecordObdj, pageable,null,null,startTime,endTime);
            }
        }else {
            gameRecordObdjPage = gameRecordObdjService.findGameRecordPage(gameRecordObdj, pageable,null,null,null,null);
        }
        PageResultVO<GameRecordObdjVo> pageResultVO =new PageResultVO(gameRecordObdjPage);
        List<GameRecordObdj> content = gameRecordObdjPage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordObdjVo> gameRecordVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(GameRecordObdj::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(gameRecord ->{
                    GameRecordObdjVo vo = new GameRecordObdjVo();
                    BeanUtils.copyProperties(gameRecord,vo);
                    vo.setWinLoss(vo.getWinAmount().subtract(vo.getBetAmount()));
                    userList.stream().forEach(u->{
                        if (u.getId().equals(gameRecord.getUserId())){
                            vo.setAccount(u.getAccount());
                        }
                    });
                    gameRecordVoList.add(vo);
                });
            }
            pageResultVO.setContent(gameRecordVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }

    /**
     * 分页查询OBDJ游戏注单
     *
     * @param user 会员账号
     * @param betId 注单号
     * @param betStatus 注单状态
     * @return
     */
    @ApiOperation("统计OBDJ游戏注单")
    @GetMapping("/findOBDJGameRecordTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betId", value = "注单号", required = false),
        @ApiImplicitParam(name = "betStatus", value = "注单状态3-待结算4-已取消5-赢(已中奖) 6-输(未中奖) 7-已撤销8-赢半9-输半10-走水", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordGoldenFTotalVo> findOBDJGameRecordTotal(String user, String betId,
        Integer betStatus,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordObdj gameRecordObdj = new GameRecordObdj();
        gameRecordObdj.setMemberAccount(user);
        gameRecordObdj.setBetStatus(betStatus);
        if (!LoginUtil.checkNull(betId)){

            try {
                Long betIdLong = Long.parseLong(betId);
                gameRecordObdj.setBetId(betIdLong);
            }catch (Exception ex){
                return ResponseUtil.custom("参数不合法");
            }
        }
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordObdj.setUserId(userId);
        GameRecordObdj content;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);

            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                content = gameRecordObdjService.findRecordRecordSum(gameRecordObdj,startTime,endTime,null,null);
            }else {
                content = gameRecordObdjService.findRecordRecordSum(gameRecordObdj,null,null,startTime,endTime);
            }
        }else {
            content = gameRecordObdjService.findRecordRecordSum(gameRecordObdj,null,null,null,null);
        }
        GameRecordGoldenFTotalVo gameRecordGoldenFTotalVo = new GameRecordGoldenFTotalVo();
        gameRecordGoldenFTotalVo.setBetAmount(content.getBetAmount());
        gameRecordGoldenFTotalVo.setWinAmount(content.getWinAmount());
        gameRecordGoldenFTotalVo.setWinLoss(content.getWinAmount().subtract(content.getBetAmount()));
        return ResponseUtil.success(gameRecordGoldenFTotalVo);
    }
    /**
     * 分页查询OBTY游戏注单
     *
     * @param user 会员账号
     * @param orderNo 注单号
     * @param outcome 订单结算结果
     * @return
     */
    @ApiOperation("分页查询OBTY游戏注单")
    @GetMapping("/findOBTYGameRecordPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
        @ApiImplicitParam(name = "outcome", value = "订单结算结果0-无结果 2-走水 3-输 4-赢 5-赢一半 6-输一半", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordObtyVo> findOBTYGameRecordPage(Integer pageSize, Integer pageCode, String user, String orderNo,
        Integer outcome,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecordObty gameRecordObty = new GameRecordObty();
        gameRecordObty.setUserName(user);
        gameRecordObty.setOutcome(outcome);
        gameRecordObty.setOrderNo(orderNo);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordObty.setUserId(userId);

        Page<GameRecordObty> gameRecordObtyPage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                gameRecordObtyPage = gameRecordObtyService.findGameRecordPage(gameRecordObty,pageable,startTime,endTime,null,null);
            }else {
                gameRecordObtyPage = gameRecordObtyService.findGameRecordPage(gameRecordObty, pageable,null,null,startTime,endTime);
            }
        }else {
            gameRecordObtyPage = gameRecordObtyService.findGameRecordPage(gameRecordObty, pageable,null,null,null,null);
        }
        PageResultVO<GameRecordObtyVo> pageResultVO =new PageResultVO(gameRecordObtyPage);
        List<GameRecordObty> content = gameRecordObtyPage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordObtyVo> gameRecordVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(GameRecordObty::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(gameRecord ->{
                    GameRecordObtyVo vo = new GameRecordObtyVo();
                    BeanUtils.copyProperties(gameRecord,vo);
                    userList.stream().forEach(u->{
                        if (u.getId().equals(gameRecord.getUserId())){
                            vo.setAccount(u.getAccount());
                        }
                    });
                    gameRecordVoList.add(vo);
                });
            }
            pageResultVO.setContent(gameRecordVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }


    /**
     * 统计OBTY游戏注单
     *
     * @param user 会员账号
     * @param orderNo 注单号
     * @param outcome 订单结算结果
     * @return
     */
    @ApiOperation("统计OBTY游戏注单")
    @GetMapping("/findOBTYGameRecordTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
        @ApiImplicitParam(name = "outcome", value = "订单结算结果0-无结果 2-走水 3-输 4-赢 5-赢一半 6-输一半", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordObtyTotalVo> findOBTYGameRecordTotal(String user, String orderNo,Integer outcome,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordObty gameRecordObty = new GameRecordObty();
        gameRecordObty.setUserName(user);
        gameRecordObty.setOutcome(outcome);
        gameRecordObty.setOrderNo(orderNo);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordObty.setUserId(userId);
        GameRecordObty content;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                content = gameRecordObtyService.findRecordRecordSum(gameRecordObty,startTime,endTime,null,null);
            }else {
                content = gameRecordObtyService.findRecordRecordSum(gameRecordObty, null,null,startTime,endTime);
            }
        }else {
            content = gameRecordObtyService.findRecordRecordSum(gameRecordObty,null,null,null,null);
        }

        GameRecordObtyTotalVo gameRecordObtyTotalVo = new GameRecordObtyTotalVo();
        gameRecordObtyTotalVo.setOrderAmount(content.getOrderAmount());
        gameRecordObtyTotalVo.setProfitAmount(content.getProfitAmount());
        gameRecordObtyTotalVo.setSettleAmount(content.getSettleAmount());
        return ResponseUtil.success(gameRecordObtyTotalVo);
    }


    /**
     * 分页查询OBZR游戏注单
     *
     * @param user 会员账号
     * @param orderNo 注单号
     * @return
     */
    @ApiOperation("分页查询OBZR游戏注单")
    @GetMapping("/findOBZRGameRecordPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordObzrVo> findOBZRGameRecordPage(Integer pageSize, Integer pageCode, String user, String orderNo,
                                                                   String account,Integer tag,
                                                                   @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                   @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecordObzr gameRecordObzr = new GameRecordObzr();
        gameRecordObzr.setPlayerName(user);
        gameRecordObzr.setOrderNo(orderNo);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordObzr.setUserId(userId);

        Page<GameRecordObzr> gameRecordObzrPage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                gameRecordObzrPage = gameRecordObzrService.findGameRecordPage(gameRecordObzr,pageable,startTime,endTime,null,null);
            }else {
                gameRecordObzrPage = gameRecordObzrService.findGameRecordPage(gameRecordObzr, pageable,null,null,startTime,endTime);
            }
        }else {
            gameRecordObzrPage = gameRecordObzrService.findGameRecordPage(gameRecordObzr, pageable,null,null,null,null);
        }
        PageResultVO<GameRecordObzrVo> pageResultVO =new PageResultVO(gameRecordObzrPage);
        List<GameRecordObzr> content = gameRecordObzrPage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordObzrVo> gameRecordVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(GameRecordObzr::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(gameRecord ->{
                    GameRecordObzrVo vo = new GameRecordObzrVo();
                    BeanUtils.copyProperties(gameRecord,vo);
                    vo.setUserName(gameRecord.getPlayerName());
                    userList.stream().forEach(u->{
                        if (u.getId().equals(gameRecord.getUserId())){
                            vo.setAccount(u.getAccount());
                        }
                    });
                    gameRecordVoList.add(vo);
                });
            }
            pageResultVO.setContent(gameRecordVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }


    /**
     * 统计OBZR游戏注单
     *
     * @param user 会员账号
     * @param orderNo 注单号
     * @return
     */
    @ApiOperation("统计OBZR游戏注单")
    @GetMapping("/findOBZRGameRecordTotal")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
            @ApiImplicitParam(name = "outcome", value = "订单结算结果0-无结果 2-走水 3-输 4-赢 5-赢一半 6-输一半", required = false),
            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordObzrTotalVo> findOBZRGameRecordTotal(String user, String orderNo,Integer outcome,String account,Integer tag,
                                                                         @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                         @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordObzr gameRecordObzr = new GameRecordObzr();
        gameRecordObzr.setPlayerName(user);
        gameRecordObzr.setOrderNo(orderNo);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordObzr.setUserId(userId);
        GameRecordObzr content;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                content = gameRecordObzrService.findRecordRecordSum(gameRecordObzr,startTime,endTime,null,null);
            }else {
                content = gameRecordObzrService.findRecordRecordSum(gameRecordObzr, null,null,startTime,endTime);
            }
        }else {
            content = gameRecordObzrService.findRecordRecordSum(gameRecordObzr,null,null,null,null);
        }

        GameRecordObzrTotalVo gameRecordObzrTotalVo = new GameRecordObzrTotalVo();
        gameRecordObzrTotalVo.setValidBetAmount(content.getValidBetAmount());
        gameRecordObzrTotalVo.setNetAmount(content.getNetAmount());
        gameRecordObzrTotalVo.setPayoutAmount(content.getPayoutAmount());
        return ResponseUtil.success(gameRecordObzrTotalVo);
    }


    /**
     * 分页查询斗鸡(SV388)游戏注单.
     *
     * @param account 会员账号
     * @param platformTxId 注单号
     * @param txStatus 订单结算结果
     * @return
     */
    @ApiOperation("分页查询斗鸡(SV388)游戏注单")
    @GetMapping("/findSV388GameRecordPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "platformTxId", value = "订单号", required = false),
        @ApiImplicitParam(name = "txStatus", value = "交易类型,-1.取消投注，0.已下注,1.已结账,2.注单无效 3.退回的金额,5.退还下注,9.无效的交易", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordAeVo> findSV388GameRecordPage(Integer pageSize, Integer pageCode, String platformTxId,
        Integer txStatus,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecordAe gameRecordAe = new GameRecordAe();
        gameRecordAe.setPlatformTxId(platformTxId);
        gameRecordAe.setTxStatus(txStatus);
        gameRecordAe.setPlatform(Constants.PLATFORM_AE_SV388);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordAe.setUserId(userId);
        return this.findPageAe(pageable,gameRecordAe,startDate,endDate,tag);
    }

    /**
     * 分页查询赛马.HORSEBOOK游戏注单.
     *
     * @param account 会员账号
     * @param platformTxId 注单号
     * @param txStatus 订单结算结果
     * @return
     */
    @ApiOperation("分页查询赛马.HORSEBOOK游戏注单")
    @GetMapping("/findHORSEBOOKGameRecordPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "platformTxId", value = "订单号", required = false),
        @ApiImplicitParam(name = "txStatus", value = "交易类型,-1.取消投注，0.已下注,1.已结账,2.注单无效 3.退回的金额,5.退还下注,9.无效的交易", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordAeVo> findHORSEBOOKGameRecordPage(Integer pageSize, Integer pageCode, String platformTxId,
        Integer txStatus,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecordAe gameRecordAe = new GameRecordAe();
        gameRecordAe.setPlatformTxId(platformTxId);
        gameRecordAe.setTxStatus(txStatus);
        gameRecordAe.setPlatform(Constants.PLATFORM_AE_HORSEBOOK);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordAe.setUserId(userId);
        return this.findPageAe(pageable,gameRecordAe,startDate,endDate,tag);
    }

    /**
     * 分页查询电竞.E1SPORT游戏注单.
     *
     * @param account 会员账号
     * @param platformTxId 注单号
     * @param txStatus 订单结算结果
     * @return
     */
    @ApiOperation("分页查询电竞.E1SPORT游戏注单")
    @GetMapping("/findE1SPORTGameRecordPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "platformTxId", value = "订单号", required = false),
        @ApiImplicitParam(name = "txStatus", value = "交易类型,-1.取消投注，0.已下注,1.已结账,2.注单无效 3.退回的金额,5.退还下注,9.无效的交易", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordAeVo> findE1SPORTGameRecordPage(Integer pageSize, Integer pageCode, String platformTxId,
        Integer txStatus,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecordAe gameRecordAe = new GameRecordAe();
        gameRecordAe.setPlatformTxId(platformTxId);
        gameRecordAe.setTxStatus(txStatus);
        gameRecordAe.setPlatform(Constants.PLATFORM_AE_E1SPORT);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordAe.setUserId(userId);
        return this.findPageAe(pageable,gameRecordAe,startDate,endDate,tag);
    }

    private ResponseEntity<GameRecordAeVo> findPageAe(Pageable pageable,GameRecordAe gameRecordAe,Date startDate,Date endDate,Integer tag){

        Page<GameRecordAe> gameRecordAePage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                gameRecordAePage = gameRecordAeService.findGameRecordAePage(gameRecordAe, pageable,startTime,endTime,null,null);
            }else {
                gameRecordAePage = gameRecordAeService.findGameRecordAePage(gameRecordAe, pageable,null,null,startTime,endTime);
            }

        }else {
            gameRecordAePage = gameRecordAeService.findGameRecordAePage(gameRecordAe, pageable,null,null,null,null);
        }
        PageResultVO<GameRecordAeVo> pageResultVO =new PageResultVO(gameRecordAePage);
        List<GameRecordAe> content = gameRecordAePage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordAeVo> gameRecordVoList = new LinkedList<>();
            List<Long> userIds = content.stream().map(GameRecordAe::getUserId).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            if(userList != null){
                content.stream().forEach(gameRecord ->{
                    GameRecordAeVo vo = new GameRecordAeVo();
                    BeanUtils.copyProperties(gameRecord,vo);
                    userList.stream().forEach(u->{
                        if (u.getId().equals(gameRecord.getUserId())){
                            vo.setAccount(u.getAccount());
                        }
                    });
                    gameRecordVoList.add(vo);
                });
            }
            pageResultVO.setContent(gameRecordVoList);
        }
        return ResponseUtil.success(pageResultVO);
    }

    /**
     * 统计斗鸡(SV388)游戏注单
     *
     * @param account 会员账号
     * @param platformTxId 注单号
     * @param txStatus 订单结算结果
     * @return
     */
    @ApiOperation("统计斗鸡(SV388)游戏注单")
    @GetMapping("/findSV388GameRecordTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "platformTxId", value = "订单号", required = false),
        @ApiImplicitParam(name = "txStatus", value = "交易类型,-1.取消投注，0.已下注,1.已结账,2.注单无效 3.退回的金额,5.退还下注,9.无效的交易", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordAeVo> findSV388GameRecordTotal(String platformTxId,Integer txStatus,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordAe gameRecordAe = new GameRecordAe();
        gameRecordAe.setPlatformTxId(platformTxId);
        gameRecordAe.setTxStatus(txStatus);
        gameRecordAe.setPlatform(Constants.PLATFORM_AE_SV388);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordAe.setUserId(userId);
        return this.sumAE(gameRecordAe,startDate,endDate,tag);
    }

    /**
     * 统计赛马.HORSEBOOK游戏注单
     *
     * @param account 会员账号
     * @param platformTxId 注单号
     * @param txStatus 订单结算结果
     * @return
     */
    @ApiOperation("统计赛马.HORSEBOOK游戏注单")
    @GetMapping("/findHORSEBOOKGameRecordTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "platformTxId", value = "订单号", required = false),
        @ApiImplicitParam(name = "txStatus", value = "交易类型,-1.取消投注，0.已下注,1.已结账,2.注单无效 3.退回的金额,5.退还下注,9.无效的交易", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordAeVo> findHORSEBOOKGameRecordTotal(String platformTxId,Integer txStatus,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordAe gameRecordAe = new GameRecordAe();
        gameRecordAe.setPlatformTxId(platformTxId);
        gameRecordAe.setTxStatus(txStatus);
        gameRecordAe.setPlatform(Constants.PLATFORM_AE_HORSEBOOK);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordAe.setUserId(userId);
        return this.sumAE(gameRecordAe,startDate,endDate,tag);
    }

    /**
     * 统计电竞.E1SPORT游戏注单
     *
     * @param account 会员账号
     * @param platformTxId 注单号
     * @param txStatus 订单结算结果
     * @return
     */
    @ApiOperation("统计电竞.E1SPORT游戏注单")
    @GetMapping("/findE1SPORTGameRecordTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "platformTxId", value = "订单号", required = false),
        @ApiImplicitParam(name = "txStatus", value = "交易类型,-1.取消投注，0.已下注,1.已结账,2.注单无效 3.退回的金额,5.退还下注,9.无效的交易", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordAeVo> findE1SPORTGameRecordTotal(String platformTxId,Integer txStatus,String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordAe gameRecordAe = new GameRecordAe();
        gameRecordAe.setPlatformTxId(platformTxId);
        gameRecordAe.setTxStatus(txStatus);
        gameRecordAe.setPlatform(Constants.PLATFORM_AE_E1SPORT);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        gameRecordAe.setUserId(userId);
        return this.sumAE(gameRecordAe,startDate,endDate,tag);
    }

    private ResponseEntity<GameRecordAeVo> sumAE(GameRecordAe gameRecordAe,Date startDate,Date endDate,Integer tag){
        GameRecordAe content;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                content = gameRecordAeService.findRecordRecordSum(gameRecordAe, startTime,endTime,null,null);
            }else {
                content = gameRecordAeService.findRecordRecordSum(gameRecordAe,null,null, startTime,endTime);
            }

        }else {
            content = gameRecordAeService.findRecordRecordSum(gameRecordAe,null,null,null,null);
        }

        GameRecordAeVo gameRecordAeVo = new GameRecordAeVo();
        gameRecordAeVo.setBetAmount(content.getBetAmount());
        gameRecordAeVo.setRealBetAmount(content.getRealBetAmount());
        gameRecordAeVo.setWinAmount(content.getWinAmount());
        gameRecordAeVo.setTurnover(content.getTurnover());
        return ResponseUtil.success(gameRecordAeVo);
    }

    @ApiOperation("分页查询越南彩注单")
    @GetMapping("/findGameRecordVNCPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "userName", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betOrder", value = "注单号", required = false),
        @ApiImplicitParam(name = "betDetailOrder", value = "注单号明细号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "gamePlay", value = "玩法类型:(0:越南玩法,1:柬埔寨玩法)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordVNCVo> findGameRecordVNCPage(Integer pageSize, Integer pageCode, String userName, String betOrder,
        String account,Integer tag,String betDetailOrder,Integer gamePlay,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        RptBetInfoDetail game = new RptBetInfoDetail();
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        game.setUserId(userId);
        game.setBetOrder(betOrder);
        game.setBetDetailOrder(betDetailOrder);
        game.setUserName(userName);
        game.setGamePlay(gamePlay);
        Page<RptBetInfoDetail> gameRecordPage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                gameRecordPage = rptBetInfoDetailService.findRptBetInfoDetailPage(game, pageable,startTime,endTime,null,null);
            }else {
                gameRecordPage = rptBetInfoDetailService.findRptBetInfoDetailPage(game, pageable,null,null,startTime,endTime);
            }
        }else {
            gameRecordPage = rptBetInfoDetailService.findRptBetInfoDetailPage(game, pageable,null,null,null,null);
        }
        PageResultVO<GameRecordVNCVo> pageResultVO =new PageResultVO(gameRecordPage);
        List<RptBetInfoDetail> content = gameRecordPage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordVNCVo> gameRecordVNCVos = new LinkedList<>();
            content.stream().forEach(gameRecord ->{
                GameRecordVNCVo vo = new GameRecordVNCVo();
                BeanUtils.copyProperties(gameRecord,vo);
                String language = TranslateConst.getLanguage();
                vo.setBetCities(TranslateConst.getBetCity(language,gameRecord.getBetCity()));
                vo.setBetPlayType(TranslateConst.getBetPlayType(language,gameRecord.getBetPlayType()));
                vo.setBetTimeStr(cn.hutool.core.date.DateUtil.formatDateTime(vo.getBetTime()));
                vo.setSettleTimeStr(cn.hutool.core.date.DateUtil.formatDateTime(vo.getSettleTime()));
                vo.setBetCategory(vo.getBetCategory() +"-"+vo.getBetPlayType());
                gameRecordVNCVos.add(vo);
            });
            pageResultVO.setContent(gameRecordVNCVos);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("分页查询大马彩注单")
    @GetMapping("/findGameRecordDMCPage")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "userName", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betOrder", value = "注单号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordDMCVo> findGameRecordDMCPage(Integer pageSize, Integer pageCode, String userName, String betOrder,
        String account,Integer tag,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecordDMC game = new GameRecordDMC();
        game.setBetOrderNo(betOrder);
        game.setUserName(userName);
        game.setUserName(account);
        Page<GameRecordDMC> gameRecordPage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                gameRecordPage = gameRecordDMCService.findGameRecordDMCPage(game, pageable,startTime,endTime,null,null);
            }else {
                gameRecordPage = gameRecordDMCService.findGameRecordDMCPage(game, pageable,null,null,startTime,endTime);
            }
        }else {
            gameRecordPage = gameRecordDMCService.findGameRecordDMCPage(game, pageable,null,null,null,null);
        }
        PageResultVO<GameRecordDMCVo> pageResultVO =new PageResultVO(gameRecordPage);
        List<GameRecordDMC> content = gameRecordPage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordDMCVo> gameRecordDMCVos = new LinkedList<>();
            content.stream().forEach(gameRecord ->{
                GameRecordDMCVo vo = new GameRecordDMCVo();
                BeanUtils.copyProperties(gameRecord,vo);
                gameRecordDMCVos.add(vo);
            });
            pageResultVO.setContent(gameRecordDMCVos);
        }
        return ResponseUtil.success(pageResultVO);
    }


    @ApiOperation("统计大马彩注单")
    @GetMapping("/findGameRecordDMCSum")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "三方会员账号", required = false),
            @ApiImplicitParam(name = "betOrder", value = "注单号", required = false),
            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordDMCVo> findGameRecordDMCSum(String userName, String betOrder,
                                                                String account,Integer tag,
                                                                @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordDMC game = new GameRecordDMC();
        game.setBetOrderNo(betOrder);
        game.setUserName(userName);
        game.setBetOrderNo(betOrder);
        GameRecordDMC recordRecordSum;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);

            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                recordRecordSum = gameRecordDMCService.findGameRecordDMCSum(game, startTime, endTime, null, null);
            }else {
                recordRecordSum = gameRecordDMCService.findGameRecordDMCSum(game,null,null,startTime,endTime);
            }
        }else {
            recordRecordSum = gameRecordDMCService.findGameRecordDMCSum(game,null,null,null,null);
        }
        GameRecordDMCVo gameRecordDMCVo = new GameRecordDMCVo();
        BeanUtils.copyProperties(recordRecordSum,gameRecordDMCVo);
        return ResponseUtil.success(gameRecordDMCVo);
    }

    @ApiOperation("统计越南彩注单")
    @GetMapping("/findGameRecordVNCSum")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userName", value = "三方会员账号", required = false),
        @ApiImplicitParam(name = "betOrder", value = "注单号", required = false),
        @ApiImplicitParam(name = "betDetailOrder", value = "注单号明细号", required = false),
        @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
        @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
        @ApiImplicitParam(name = "gamePlay", value = "玩法类型:(0:越南玩法,1:柬埔寨玩法)", required = false),
        @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordVNCVo> findGameRecordVNCSum(String userName, String betOrder,
        String account,Integer tag,String betDetailOrder,Integer gamePlay,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        RptBetInfoDetail game = new RptBetInfoDetail();
        game.setBetOrder(betOrder);
        game.setBetDetailOrder(betDetailOrder);
        game.setUserName(userName);
        Long userId = null;
        if (!LoginUtil.checkNull(account)){
            User byAccount = userService.findByAccount(account);
            if (LoginUtil.checkNull(byAccount)){
                return ResponseUtil.custom("用户不存在");
            }
            userId = byAccount.getId();
        }
        game.setUserId(userId);
        game.setGamePlay(gamePlay);
        RptBetInfoDetail recordRecordSum;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);

            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                recordRecordSum = rptBetInfoDetailService.findRptBetInfoDetailSum(game, startTime, endTime, null, null);
            }else {
                recordRecordSum = rptBetInfoDetailService.findRptBetInfoDetailSum(game,null,null,startTime,endTime);
            }
        }else {
            recordRecordSum = rptBetInfoDetailService.findRptBetInfoDetailSum(game,null,null,null,null);
        }
        GameRecordVNCVo gameRecordVNCVo = new GameRecordVNCVo();
        BeanUtils.copyProperties(recordRecordSum,gameRecordVNCVo);
        return ResponseUtil.success(gameRecordVNCVo);
    }
    @ApiOperation("分页查询DG注单")
    @GetMapping("/findGameRecordDGPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "userName", value = "三方会员账号", required = false),
            @ApiImplicitParam(name = "betOrder", value = "注单号", required = false),
            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordDGVo> findGameRecordDGPage(Integer pageSize, Integer pageCode, String userName, String betOrder,
                                                                 String account,Integer tag,
                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        GameRecordDG game = new GameRecordDG();
        game.setBetOrderNo(betOrder);
        game.setUserName(userName);
        game.setUserAcct(account);
        Page<GameRecordDG> gameRecordPage;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                gameRecordPage = gameRecordDGService.findGameRecordDGPage(game, pageable,startTime,endTime,null,null);
            }else {
                gameRecordPage = gameRecordDGService.findGameRecordDGPage(game, pageable,null,null,startTime,endTime);
            }
        }else {
            gameRecordPage = gameRecordDGService.findGameRecordDGPage(game, pageable,null,null,null,null);
        }
        PageResultVO<GameRecordDGVo> pageResultVO =new PageResultVO(gameRecordPage);
        List<GameRecordDG> content = gameRecordPage.getContent();
        if(content != null && content.size() > 0){
            List<GameRecordDGVo> gameRecordDGVos = new LinkedList<>();
            content.stream().forEach(gameRecord ->{
                GameRecordDGVo vo = new GameRecordDGVo();
                //结算时间字符串(下注多城市情况下,记入最后结算城市)
                BeanUtils.copyProperties(gameRecord,vo);
                gameRecordDGVos.add(vo);
            });
            pageResultVO.setContent(gameRecordDGVos);
        }
        return ResponseUtil.success(pageResultVO);
    }


    @ApiOperation("统计DG注单")
    @GetMapping("/findGameRecordDGSum")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "三方会员账号", required = false),
            @ApiImplicitParam(name = "betOrder", value = "注单号", required = false),
            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<GameRecordDGVo> findGameRecordDGSum(String userName, String betOrder,
                                                                String account,Integer tag,
                                                                @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        GameRecordDG game = new GameRecordDG();
        game.setBetOrderNo(betOrder);
        game.setUserName(userName);
        GameRecordDG recordRecordSum;
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);

            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                recordRecordSum = gameRecordDGService.findGameRecordDGSum(game, startTime, endTime, null, null);
            }else {
                recordRecordSum = gameRecordDGService.findGameRecordDGSum(game,null,null,startTime,endTime);
            }
        }else {
            recordRecordSum = gameRecordDGService.findGameRecordDGSum(game,null,null,null,null);
        }
        GameRecordDGVo gameRecordDGVo = new GameRecordDGVo();
        BeanUtils.copyProperties(recordRecordSum,gameRecordDGVo);
        return ResponseUtil.success(gameRecordDGVo);
    }
    //    @ApiOperation("分页查询三方游戏注单总计")
    //    @GetMapping("/findGameRecordTotal")
    //    @ApiImplicitParams({
    //            @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
    //            @ApiImplicitParam(name = "betId", value = "注单号", required = false),
    //            @ApiImplicitParam(name = "platformType", value = "平台类型 WM，OBTY，OBDJ", required = true),
    //            @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
    //            @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
    //            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
    //            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
    //            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
    //            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    //    })
    //    @NoAuthorization
    //    public ResponseEntity<GameRecordTotalVo> findGameRecordTotal(String user, String betId, String platformType,
    //                                                                 String gname, Integer gid, String account, Integer tag,
    //                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
    //                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
    //
    //        if(LoginUtil.checkNull(platformType)){//默认查询WM
    //            platformType = Constants.PLATFORM_WM_BIG;
    //        }
    //        //1：查询参数处理
    //        GameRecord game = getGameUserRecord(account, betId, gname, user);
    //        if(game == null){
    //            return ResponseUtil.success();
    //        }
    //        GameRecord recordRecordSum = null;
    //        //2：结果查询
    //        switch (platformType){
    //
    //            case Constants.PLATFORM_OBDJ :
    //                recordRecordSum = getOBDJTotal(startDate, endDate, game, tag);
    //                break;
    //            case Constants.PLATFORM_OBTY :
    //                recordRecordSum = getOBTYTotal(startDate, endDate, game, tag);
    //                break;
    //            default:
    //                recordRecordSum = getWMTotal(startDate, endDate, game, tag);
    //                break;
    //
    //        }
    //        GameRecordTotalVo gameRecordTotalVo = new GameRecordTotalVo();
    //        if (!LoginUtil.checkNull(recordRecordSum)){
    //            if (StringUtils.hasText(recordRecordSum.getBet())){
    //                gameRecordTotalVo.setBet(new BigDecimal(recordRecordSum.getBet()));
    //            }
    //            if (StringUtils.hasText(recordRecordSum.getValidbet())){
    //                gameRecordTotalVo.setValidbet(new BigDecimal(recordRecordSum.getValidbet()));
    //            }
    //            if (StringUtils.hasText(recordRecordSum.getBet())){
    //                gameRecordTotalVo.setWinLoss(new BigDecimal(recordRecordSum.getWinLoss()));
    //            }
    //        }
    //        return ResponseUtil.success(gameRecordTotalVo);
    //
    //    }

    //    private GameRecord getOBDJTotal(Date startDate, Date endDate, GameRecord game, Integer tag) {
    //        GameRecordObdj content;
    //        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
    //            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
    //            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
    //
    //            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
    //                content = gameRecordObdjService.findRecordRecordSum(game,startTime,endTime,null,null);
    //            }else {
    //                content = gameRecordObdjService.findRecordRecordSum(game,null,null,startTime,endTime);
    //            }
    //        }else {
    //            content = gameRecordObdjService.findRecordRecordSum(game,null,null,null,null);
    //        }
    //
    //        GameRecord gameRecord = new GameRecord();
    //        gameRecord.setBet(content.getBetAmount().toPlainString());
    //        gameRecord.setValidbet(content.getBetAmount().toPlainString());
    //        gameRecord.setWinLoss(content.getWinAmount().toPlainString());
    //        return gameRecord;
    //    }

    //    private GameRecord getOBTYTotal(Date startDate, Date endDate, GameRecord game, Integer tag) {
    //        GameRecordObty content;
    //        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
    //            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
    //            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
    //            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
    //                content = gameRecordObtyService.findRecordRecordSum(game,startTime,endTime,null,null);
    //            }else {
    //                content = gameRecordObtyService.findRecordRecordSum(game, null,null,startTime,endTime);
    //            }
    //        }else {
    //            content = gameRecordObtyService.findRecordRecordSum(game,null,null,null,null);
    //        }
    //        GameRecord gameRecord = new GameRecord();
    //        gameRecord.setBet(content.getPreBetAmount().toPlainString());
    //        gameRecord.setValidbet(content.getOrderAmount().toPlainString());
    //        gameRecord.setWinLoss(content.getSettleAmount().toPlainString());
    //        return gameRecord;
    //    }

    //    private GameRecord getWMTotal(Date startDate, Date endDate, GameRecord game, Integer tag) {
    //        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
    //            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
    //            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
    //            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
    //                return gameRecordService.findRecordRecordSum(game,startTime,endTime,null,null);
    //            }else {
    //                return gameRecordService.findRecordRecordSum(game,null,null,startTime,endTime);
    //            }
    //        }else {
    //            return gameRecordService.findRecordRecordSum(game,null,null,null,null);
    //        }
    //    }

    //    private PageResultVO<GameRecordVo> getOBTY(Date startDate, Date endDate, GameRecord game, Pageable pageable, Integer tag) {
    //        List<GameRecordVo> gameRecordVoList;
    //        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
    //            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
    //            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
    //            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
    //                Page<GameRecordObty> page = gameRecordObtyService.findGameRecordPage(game, pageable,startTime,endTime,null,null);
    //                gameRecordVoList = getGameRecordTYVoList(page.getContent());
    //                return new PageResultVO<GameRecordVo>(page.getNumber(), page.getSize(), page.getTotalElements(), gameRecordVoList);
    //            }else {
    //                Page<GameRecordObty> page = gameRecordObtyService.findGameRecordPage(game, pageable,null,null,startTime,endTime);
    //                gameRecordVoList = getGameRecordTYVoList(page.getContent());
    //                return new PageResultVO<GameRecordVo>(page.getNumber(), page.getSize(), page.getTotalElements(), gameRecordVoList);
    //            }
    //        }else {
    //            Page<GameRecordObty> page = gameRecordObtyService.findGameRecordPage(game, pageable,null,null,null,null);
    //            gameRecordVoList = getGameRecordTYVoList(page.getContent());
    //            return new PageResultVO<GameRecordVo>(page.getNumber(), page.getSize(), page.getTotalElements(), gameRecordVoList);
    //        }
    //    }

    //    private List<GameRecordVo> getGameRecordTYVoList(List<GameRecordObty> content) {
    //        List<GameRecordVo> gameRecordVoList = new ArrayList<>();
    //        if(content.isEmpty()){
    //            return gameRecordVoList;
    //        }
    //        for (GameRecordObty gameRecordObty : content) {
    //            GameRecordVo gameRecordVo = new GameRecordVo();
    //            gameRecordVo.setId(gameRecordObty.getId());
    //            gameRecordVo.setUserId(gameRecordObty.getUserId());
    //            gameRecordVo.setUser(gameRecordObty.getUserName());
    //            gameRecordVo.setBetId(gameRecordObty.getOrderNo() + "");
    //            gameRecordVo.setBetTime(gameRecordObty.getBetStrTime());
    //            gameRecordVo.setBet(gameRecordObty.getOrderAmount().toPlainString());
    //            gameRecordVo.setGname("OBDJ");
    //            gameRecordVo.setBetResult(gameRecordObty.getSeriesValue());
    //            gameRecordVo.setValidbet(gameRecordObty.getOrderAmount().toPlainString());
    //            gameRecordVo.setSettime(gameRecordObty.getSettleStrTime());
    //            gameRecordVo.setWinLoss(gameRecordObty.getProfitAmount().toPlainString());
    //            gameRecordVo.setFirstProxy(gameRecordObty.getFirstProxy());
    //            gameRecordVo.setSecondProxy(gameRecordObty.getSecondProxy());
    //            gameRecordVo.setThirdProxy(gameRecordObty.getThirdProxy());
    //            gameRecordVoList.add(gameRecordVo);
    //        }
    //        return gameRecordVoList;
    //    }


    //    /**
    //     * 设置用户重要信息
    //     *
    //     * @param pageResultVO
    //     */
    //    private void setUserImportantUser(PageResultVO<GameRecordVo> pageResultVO, PageResultVO<GameRecordVo> gameRecordPage) {
    //        List<GameRecordVo> content = (List<GameRecordVo>) gameRecordPage.getContent();
    //        if(content != null && content.size() > 0){
    //            List<GameRecordVo> gameRecordVoList = new LinkedList<>();
    //            List<Long> userIds = content.stream().map(GameRecordVo::getUserId).collect(Collectors.toList());
    //            List<User> userList = userService.findAll(userIds);
    //            if(userList != null){
    //                content.stream().forEach(gameRecordvo ->{
    //
    //                    userList.stream().forEach(u->{
    //                        if (u.getId().equals(gameRecordvo.getUserId())){
    //                            gameRecordvo.setAccount(u.getAccount());
    //                        }
    //                    });
    //                    gameRecordVoList.add(gameRecordvo);
    //                });
    //            }
    //            pageResultVO.setContent(gameRecordVoList);
    //        }
    //
    //    }

    //    /**
    //     * 查询调节
    //     *
    //     * @param account
    //     * @param betId
    //     * @param gname
    //     * @param thridName
    //     * @return
    //     */
    //    private GameRecord getGameUserRecord(String account, String betId, String gname, String thridName) {
    //        GameRecord game = new GameRecord();
    //        Long userId = null;
    //        if (!LoginUtil.checkNull(account)){
    //            User byAccount = userService.findByAccount(account);
    //            if (LoginUtil.checkNull(byAccount)){
    //                return null;
    //            }
    //            userId = byAccount.getId();
    //        }
    //        game.setUserId(userId);
    //        game.setBetId(betId);
    //        game.setGname(gname);
    //        game.setUser(thridName);
    //        return game;
    //    }

    //    /**
    //     * 查询OBDJ数据
    //     *
    //     * @param startDate
    //     * @param endDate
    //     * @param game
    //     * @param pageable
    //     * @param tag
    //     * @return
    //     */
    //    private PageResultVO<GameRecordVo> getOBDJ(Date startDate, Date endDate, GameRecord game, Pageable pageable, Integer tag) {
    //        List<GameRecordVo> gameRecordVoList;
    //        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
    //            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
    //            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
    //            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
    //                Page<GameRecordObdj> page = gameRecordObdjService.findGameRecordPage(game, pageable,startTime,endTime,null,null);
    //                gameRecordVoList = getGameRecordVoList(page.getContent());
    //                return new PageResultVO<GameRecordVo>(page.getNumber(), page.getSize(), page.getTotalElements(), gameRecordVoList);
    //            }else {
    //                Page<GameRecordObdj> page = gameRecordObdjService.findGameRecordPage(game, pageable,null,null,startTime,endTime);
    //                gameRecordVoList = getGameRecordVoList(page.getContent());
    //                return new PageResultVO<GameRecordVo>(page.getNumber(), page.getSize(), page.getTotalElements(), gameRecordVoList);
    //            }
    //        }else {
    //            Page<GameRecordObdj> page = gameRecordObdjService.findGameRecordPage(game, pageable,null,null,null,null);
    //            gameRecordVoList = getGameRecordVoList(page.getContent());
    //            return new PageResultVO<GameRecordVo>(page.getNumber(), page.getSize(), page.getTotalElements(), gameRecordVoList);
    //        }
    //    }

    private List<GameRecordVo> getGameRecordVoList(List<GameRecordObdj> content) {
        List<GameRecordVo> gameRecordVoList = new ArrayList<>();
        if(content.isEmpty()){
            return gameRecordVoList;
        }
        for (GameRecordObdj gameRecordObdj : content) {
            GameRecordVo gameRecordVo = new GameRecordVo();
            gameRecordVo.setId(gameRecordObdj.getId());
            gameRecordVo.setUserId(gameRecordObdj.getUserId());
            gameRecordVo.setUser(gameRecordObdj.getMemberAccount());
            gameRecordVo.setBetId(gameRecordObdj.getBetId() + "");
            gameRecordVo.setBetTime(gameRecordObdj.getBetStrTime());
            gameRecordVo.setBet(gameRecordObdj.getBetAmount().toPlainString());
            gameRecordVo.setGname("OBDJ");
            gameRecordVo.setBetResult(gameRecordObdj.getOddName());
            gameRecordVo.setValidbet(gameRecordObdj.getBetAmount().toPlainString());
            gameRecordVo.setSettime(gameRecordObdj.getSetStrTime());
            gameRecordVo.setWinLoss(gameRecordObdj.getWinAmount().toPlainString());
            gameRecordVo.setFirstProxy(gameRecordObdj.getFirstProxy());
            gameRecordVo.setSecondProxy(gameRecordObdj.getSecondProxy());
            gameRecordVo.setThirdProxy(gameRecordObdj.getThirdProxy());
            gameRecordVoList.add(gameRecordVo);
        }
        return gameRecordVoList;
    }


    /**
     * 查询WM注单记录
     *
     * @param startDate
     * @param endDate
     * @param game
     * @param pageable
     * @return
     */
    private PageResultVO<GameRecordVo> getWM(Date startDate, Date endDate, GameRecord game, Pageable pageable, Integer tag) {
        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
                Page<GameRecord> gameRecordPage = gameRecordService.findGameRecordPage(game, pageable, startTime, endTime, null, null);
                List<GameRecordVo> gameRecordVoList = getGameRecordWMVoList(gameRecordPage.getContent());
                return new PageResultVO(gameRecordPage.getNumber(), gameRecordPage.getSize(), gameRecordPage.getTotalElements(), gameRecordVoList);
            }else {
                Page<GameRecord> gameRecordPage = gameRecordService.findGameRecordPage(game, pageable, null, null, startTime, endTime);
                List<GameRecordVo> gameRecordVoList = getGameRecordWMVoList(gameRecordPage.getContent());
                return new PageResultVO(gameRecordPage.getNumber(), gameRecordPage.getSize(), gameRecordPage.getTotalElements(), gameRecordVoList);

            }
        }else {
            Page<GameRecord> gameRecordPage = gameRecordService.findGameRecordPage(game, pageable, null, null, null, null);
            List<GameRecordVo> gameRecordVoList = getGameRecordWMVoList(gameRecordPage.getContent());
            return new PageResultVO(gameRecordPage.getNumber(), gameRecordPage.getSize(), gameRecordPage.getTotalElements(), gameRecordVoList);

        }

    }

    private List<GameRecordVo> getGameRecordWMVoList(List<GameRecord> content) {
        List<GameRecordVo> gameRecordVoList = new ArrayList<>();
        if(content.isEmpty()){
            return gameRecordVoList;
        }
        for (GameRecord gameRecord : content) {
            GameRecordVo gameRecordVo = new GameRecordVo(gameRecord);
            gameRecordVoList.add(gameRecordVo);
        }

        return gameRecordVoList;
    }

    //    @ApiOperation("分页查询三方游戏注单总计")
    //    @GetMapping("/findGameRecordTotal1")
    //    @ApiImplicitParams({
    //            @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
    //            @ApiImplicitParam(name = "betId", value = "注单号", required = false),
    //            @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
    //            @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
    //            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
    //            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
    //            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
    //            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    //    })
    //    @NoAuthorization
    //    public ResponseEntity<GameRecordTotalVo> findGameRecordTotal1(String user, String betId,
    //                                                                 String gname, Integer gid, String account, Integer tag,
    //                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
    //                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
    //        GameRecord game = new GameRecord();
    //        Long userId = null;
    //        if (!LoginUtil.checkNull(account)){
    //            User byAccount = userService.findByAccount(account);
    //            if (LoginUtil.checkNull(byAccount)){
    //                return ResponseUtil.custom("用户不存在");
    //            }
    //            userId = byAccount.getId();
    //        }
    //        game.setUserId(userId);
    //        game.setBetId(betId);
    //        game.setGname(gname);
    //        game.setGid(gid);
    //        game.setUser(user);
    //        List<GameRecord> gameRecordList;
    //        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
    //            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
    //            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
    //            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
    //                gameRecordList = gameRecordService.findGameRecordList(game,startTime,endTime,null,null);
    //            }else {
    //                gameRecordList = gameRecordService.findGameRecordList(game,null,null,startTime,endTime);
    //            }
    //        }else {
    //            gameRecordList = gameRecordService.findGameRecordList(game,null,null,null,null);
    //        }
    //        BigDecimal bet=BigDecimal.ZERO;
    //        BigDecimal validbet=BigDecimal.ZERO;
    //        BigDecimal winLoss=BigDecimal.ZERO;
    //        if(gameRecordList != null && gameRecordList.size() > 0){
    //            List<Map<BigDecimal, Object>> list = new ArrayList<>();
    //            gameRecordList.stream().forEach(info ->{
    //                list.add(new HashMap(1) {{
    //                    put("validbet", new BigDecimal(info.getValidbet()));
    //                    put("bet", new BigDecimal(info.getBet()));
    //                    put("winLoss", new BigDecimal(info.getWinLoss()));
    //                }});
    //            });
    //             bet = list.stream().map(item -> { { return new BigDecimal(item.get("bet").toString());}}).reduce(BigDecimal.ZERO, BigDecimal::add);
    //            validbet = list.stream().map(item -> { { return new BigDecimal(item.get("validbet").toString());}}).reduce(BigDecimal.ZERO, BigDecimal::add);
    //            winLoss = list.stream().map(item -> { { return new BigDecimal(item.get("winLoss").toString());}}).reduce(BigDecimal.ZERO, BigDecimal::add);
    //        }
    //        GameRecordTotalVo gameRecordTotalVo=new GameRecordTotalVo();
    //        gameRecordTotalVo.setBet(bet);
    //        gameRecordTotalVo.setValidbet(validbet);
    //        gameRecordTotalVo.setWinLoss(winLoss);
    //        return ResponseUtil.success(gameRecordTotalVo);
    //    }

    //    /**
    //     * 统计三方游戏注单
    //     *
    //     * @param user 会员账号
    //     * @param betId 注单号
    //     * @param gname 游戏名称
    //     * @param gid 游戏类型
    //     * @return
    //     */
    //    @ApiOperation("统计三方游戏注单")
    //    @GetMapping("/findRecordRecordSum")
    //    @ApiImplicitParams({
    //            @ApiImplicitParam(name = "user", value = "三方会员账号", required = false),
    //            @ApiImplicitParam(name = "account", value = "我方会员账号", required = false),
    //            @ApiImplicitParam(name = "betId", value = "注单号", required = false),
    //            @ApiImplicitParam(name = "gname", value = "游戏名称", required = false),
    //            @ApiImplicitParam(name = "gid", value = "游戏类型", required = false),
    //            @ApiImplicitParam(name = "tag", value = "查询时间类型(0按照投注 1按照结算)", required = false),
    //            @ApiImplicitParam(name = "startDate", value = "查询起始时间查询", required = false),
    //            @ApiImplicitParam(name = "endDate", value = "查询结束时间查询", required = false),
    //    })
    //    @NoAuthorization
    //    public ResponseEntity findRecordRecordSum(String user,String betId,String gname,Integer gid,String account,Integer tag,
    //                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
    //                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
    //        GameRecord game = new GameRecord();
    //        if (!LoginUtil.checkNull(account)){
    //            User byAccount = userService.findByAccount(account);
    //            if (LoginUtil.checkNull(byAccount)){
    //                return ResponseUtil.custom("用户不存在");
    //            }
    //            game.setUserId(byAccount.getId());
    //        }
    //        game.setUser(user);
    //        game.setBetId(betId);
    //        game.setGname(gname);
    //        game.setGid(gid);
    //        if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
    //            String startTime = DateUtil.getSimpleDateFormat().format(startDate);
    //            String endTime = DateUtil.getSimpleDateFormat().format(endDate);
    //            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_0){
    //                return ResponseUtil.success(gameRecordService.findRecordRecordSum(game,startTime,endTime,null,null));
    //            }else {
    //                return ResponseUtil.success(gameRecordService.findRecordRecordSum(game,null,null,startTime,endTime));
    //            }
    //        }else {
    //            return ResponseUtil.success(gameRecordService.findRecordRecordSum(game,null,null,null,null));
    //        }
    //
    //    }
}