package com.qianyi.casinocore.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.mysql.cj.util.StringUtils;
import com.qianyi.casinocore.co.user.LevelChangeCo;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.repository.UserLevelRepository;
import com.qianyi.casinocore.util.*;
import com.qianyi.casinocore.vo.*;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@CacheConfig(cacheNames = {"userLevel"})
@Slf4j
public class UserLevelService {

    @Autowired
    UserLevelRepository userLevelRepository;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private LevelWaterChangeService levelWaterChangeService;
    @Autowired
    private UserService userService;
    @Autowired
    private WashCodeChangeService washCodeChangeService;

    @PersistenceContext
    private EntityManager entityManager;

    public UserLevelRecord save(UserLevelRecord userLevel) {
        return userLevelRepository.save(userLevel);
    }


    public UserLevelRecord queryKeepLevel(UserLevelRecord userLevel) {
        return userLevelRepository.save(userLevel);
    }

    public UserLevelRecord findByUserId(Long userId) {
        return userLevelRepository.findByUserId(userId);
    }

    public List<UserLevelRecord> findUserLevel(List<Long> userIds) {
        Specification<UserLevelRecord> condition = getConditionId(userIds);
        List<UserLevelRecord> proxyUserList = userLevelRepository.findAll(condition);
        proxyUserList.stream().collect(Collectors.groupingBy(UserLevelRecord::getUserId, Collectors.maxBy(Comparator.comparing(UserLevelRecord::getUpdateTime)))).forEach((k, v) -> {
            System.out.println("K:" + k + "-V:" + v.get());
        });
        return proxyUserList;
    }

    public void processLevelWater(User user, UserVo userVo) {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        Integer upgradeBet = LevelUtil.getUpgradeBet(platformConfig, user.getLevel());
        Integer keepBet = LevelUtil.getKeepBet(platformConfig, user.getLevel());
        UserMoney userMoney = userMoneyService.findByUserId(user.getId());
        BigDecimal riseWater = null;
        if (Objects.nonNull(userMoney)){
            riseWater = userMoney.getRiseWater()==null?BigDecimal.ZERO:userMoney.getRiseWater();
        }else {
            log.error("找不到用户钱包:{}",user.getId());
            riseWater = BigDecimal.ZERO;
        }
        if (user.getLevel() >= 10) {
            riseWater = new BigDecimal(upgradeBet);
        }
        userVo.setPromote(riseWater + "/" + new BigDecimal(upgradeBet));
        BigDecimal levelWater = userVo.getLevelWater() == null ? BigDecimal.ZERO : userVo.getLevelWater();
        if (levelWater.subtract(new BigDecimal(keepBet)).intValue() > 0) {
            levelWater = new BigDecimal(keepBet);
        }
        userVo.setKeep(levelWater + "/" + new BigDecimal(keepBet));
    }

    public void processLevelWater2(UserLevelRecord change) {
//        PlatformConfig platformConfig = platformConfigService.findFirst();
//        Integer upgradeBet = LevelUtil.getUpgradeBet(platformConfig, change.getLevel());
//        Integer keepBet = LevelUtil.getKeepBet(platformConfig, change.getLevel());
        if (change.getChangeType().equals(1)) {
            change.setSchedule(change.getSchedule());
        } else {
            change.setSchedule(change.getSchedule());
        }

    }


    public UserLevelRecord findDropRecord(Long userId, Integer level) {
        return userLevelRepository.findDropRecord(userId, level);
    }

    public List<Map<String, Object>> findLastRiseUser(Date startTime, Date endTime) {
        SimpleDateFormat format = DateUtil.getSimpleDateFormat();
        return userLevelRepository.findLastRiseUser(format.format(startTime), format.format(endTime));
    }


    public Page<UserLevelRecord> findLevelChangePage(Pageable pageable, UserLevelRecord userLevel) {
        Specification<UserLevelRecord> condition = this.getCondition(userLevel);
        Page<UserLevelRecord> all = userLevelRepository.findAll(condition, pageable);
        return all;
    }

    private Specification<UserLevelRecord> getConditionId(List<Long> userIds) {
        Specification<UserLevelRecord> specification = (root, criteriaQuery, cb) -> {
            List<Predicate> list = new ArrayList<>();
            if (userIds != null && userIds.size() > 0) {
                Path<Object> userId = root.get("id");
                CriteriaBuilder.In<Object> in = cb.in(userId);
                for (Long id : userIds) {
                    in.value(id);
                }
                list.add(cb.and(cb.and(in)));
            }
            return cb.and(list.toArray(new Predicate[list.size()]));
        };
        return specification;
    }


    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param
     * @return
     */
    private Specification<UserLevelRecord> getCondition(UserLevelRecord userLevel) {
        Specification<UserLevelRecord> specification = (root, criteriaQuery, cb) -> {
            Predicate predicate;
            List<Predicate> list = new ArrayList<>();
            if (userLevel.getUserId() != null) {
                list.add(cb.equal(root.get("userId").as(Long.class), userLevel.getUserId()));
            }
            predicate = cb.and(list.toArray(new Predicate[list.size()]));
            return predicate;
        };
        return specification;
    }

    public void levelChange(LevelChangeCo levelChangeCo) {
        try {
            LevelWaterChange levelWaterChange = new LevelWaterChange();
            levelWaterChange.setBetWater(levelChangeCo.getBetWater());
            levelWaterChange.setUserId(levelChangeCo.getUserId());
            GameRecord gameRecord = levelChangeCo.getGameRecord();
            if (Constants.PLATFORM_WM.equals(levelChangeCo.getPlatform())) {
                levelWaterChange.setGameId(gameRecord.getGid().toString());
            } else {
                levelWaterChange.setGameId(gameRecord.getGameCode());
            }
            LevelWaterChange waterChange = levelWaterChangeService.findByGameRecordId(gameRecord.getId());
            if (ObjectUtil.isNotNull(waterChange) && levelChangeCo.getPlatform().equals(waterChange.getGameId())) {
                log.error("订单已处理 ===== >> levelChangeCo {} ,错误信息 {} ", JSON.toJSONString(levelChangeCo));
                throw new RuntimeException("等级流水变动异常");
            }
            levelWaterChange.setGameName(gameRecord.getGname());
            levelWaterChange.setGameRecordId(gameRecord.getId());
            levelWaterChange.setUserId(levelChangeCo.getUserId());
            levelWaterChangeService.save(levelWaterChange);
            if (levelChangeCo.getTradeType() == 1) {
                userMoneyService.addRiseWater(levelChangeCo.getUserId(), levelChangeCo.getBetWater());
                userMoneyService.addLevelWater(levelChangeCo.getUserId(), levelChangeCo.getBetWater());
            } else {
                UserMoney userMoneyLock2 = userMoneyService.findUserByUserIdUseLock(levelChangeCo.getUserId());
                userMoneyService.subRiseWater(levelChangeCo.getUserId(), levelChangeCo.getBetWater(), userMoneyLock2);
                userMoneyService.subLevelWater(levelChangeCo.getUserId(), levelChangeCo.getBetWater(), userMoneyLock2);
            }
        } catch (Exception e) {
            log.error("等级流水变动异常 ===== >> levelChangeCo {} ,错误信息 {} ", JSON.toJSONString(levelChangeCo), e);
            throw new RuntimeException("等级流水变动异常");
        }

    }

    public Integer findVipMapCount(String levelArray, Long userId) {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(" SELECT count(1) FROM `user` WHERE 1 = 1 ");
        if (StrUtil.isNotBlank(levelArray)) {
            stringBuffer.append(" and   `level` IN (" + levelArray + ") ");
        }
        if (ObjectUtil.isNotNull(userId)) {
            stringBuffer.append(" and   `id` = " + userId + " ");
        }
        String sql = stringBuffer.toString();
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object countResult = countQuery.getSingleResult();
        String str = countResult.toString();
        return Integer.parseInt(str);
    }


    public List<VipReportVo> findVipMap(String startTime, String endTime, String levelArray, Long userId, Integer pageCode, Integer pageSize) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(" SELECT   u.*, k.* ,IFNULL(td.todayAward,0.000) todayAward , IFNULL(rs.riseAward,0.000)  asriseAward  " +
                "           FROM   " +
                "            (   " +
                "             SELECT   " +
                "              account,   " +
                "              id,   " +
                "              `level`,   " +
                "              create_time as createTime   " +
                "             FROM   " +
                "              `user`   " +
                "             WHERE 1=1    ");
        if (StrUtil.isNotBlank(levelArray)) {
            stringBuffer.append(" and   `level` IN (" + levelArray + ") ");
        }
        if (ObjectUtil.isNotNull(userId)) {
            stringBuffer.append(" and   `id` = " + userId + " ");
        }
        stringBuffer.append(" ) u ");
        stringBuffer.append(" INNER JOIN ( " +
                " SELECT " +
                " user_id as userId, " +
                " SUM(betting_number) num, " +
                " sum(bet_amount) betAmount, " +
                " sum(valid_amount) validBet, " +
                " sum(win_loss) winLoss " +
                " FROM " +
                " proxy_game_record_report gr ");
        if (StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)) {
            stringBuffer.append("   where order_times BETWEEN" + startTime + " AND " + endTime + "");
        }
        stringBuffer.append("GROUP BY  user_id ) k ON k.userId = u.id ");
        stringBuffer.append("   LEFT JOIN ( " +
                "                SELECT " +
                "        user_id, " +
                "                SUM(amount) AS todayAward " +
                "        FROM " +
                "                award_receive_record " +
                "        WHERE " +
                "        1 = 1 " +
                "        AND award_type = 1 ");
        if (StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)) {
            stringBuffer.append("   AND create_time BETWEEN" + startTime + " AND " + endTime + "");
        }
        stringBuffer.append("GROUP BY  user_id ) td ON u.id = td.user_id ");
        stringBuffer.append("LEFT JOIN ( " +
                " SELECT " +
                " user_id, " +
                " SUM(amount) AS riseAward " +
                " FROM " +
                " award_receive_record " +
                " WHERE " +
                " 1 = 1 " +
                " AND award_type = 2");
        if (StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)) {
            stringBuffer.append("   AND receive_time BETWEEN" + startTime + " AND " + endTime + "");
        }
        stringBuffer.append("  GROUP BY user_id ) rs ON u.id = td.user_id ");
        stringBuffer.append("  ORDER BY   u.createTime DESC   LIMIT " + pageCode + "," + pageSize + " ");
        String sql = stringBuffer.toString();
        log.info(sql);
        List<String> list = VIP_REPORT_VO_FIELD_LIST;
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        if (null != resultList && resultList.size() > 0) {
            List<Map<String, Object>> mapList = parsePersonReportMapList(resultList, list);
            return DTOUtil.map2DTO(mapList, VipReportVo.class);
        } else {
            return null;
        }
    }

    public Map<String, Object> findVipTotalMap(String startTime, String endTime, String levelArray, Long userId) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String userIdsql = "";
        List<String> list = VIP_TOTAL_REPORT_VO_FIELD_LIST;
        StringBuffer userIdSb = new StringBuffer();
        userIdSb.append(" SELECT id from `user` WHERE 1=1 ");
        if (StrUtil.isNotBlank(levelArray)) {
            userIdSb.append(" and   `level` IN (" + levelArray + ") ");
        }
        if (ObjectUtil.isNotNull(userId)) {
            userIdSb.append(" and   `id` = " + userId + " ");
        }
        userIdsql = userIdSb.toString();

        String reportSql = "";
        StringBuffer reportSb = new StringBuffer();
        reportSb.append("SELECT\n" +
                "\t  a.validBet,\ta.winLoss,b.todayAward,\tc.riseAward \n" +
                "FROM\n" +
                "\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tuser_id,\n" +
                "\t\t\tSUM(betting_number) num,\n" +
                "\t\t\tsum(valid_amount) validBet,\n" +
                "\t\t\tsum(win_loss) winLoss\n" +
                "\t\tFROM\n" +
                "\t\t\tproxy_game_record_report gr\n" +
                "\t\tWHERE\n" +
                "\t\t\t1 = 1 ");
        if (StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)) {
            reportSb.append("   AND order_times BETWEEN" + startTime + " AND " + endTime + "");
        }
        if (StrUtil.isNotBlank(userIdsql)) {
            reportSb.append("   AND user_id IN (" + userIdsql + ") ");
        }
        reportSb.append(" \t) a, ");
        reportSb.append("\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tSUM(amount) AS todayAward\n" +
                "\t\tFROM\n" +
                "\t\t\taward_receive_record\n" +
                "\t\tWHERE\n" +
                "\t\t\taward_type = 1 ");
        if (StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)) {
            reportSb.append("   AND create_time BETWEEN" + startTime + " AND " + endTime + "");
        }
        if (StrUtil.isNotBlank(userIdsql)) {
            reportSb.append("   AND user_id IN (" + userIdsql + ") ");
        }
        reportSb.append(" \t) b, ");
        reportSb.append("\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tSUM(amount) AS riseAward\n" +
                "\t\tFROM\n" +
                "\t\t\taward_receive_record\n" +
                "\t\tWHERE\n" +
                "\t\t\taward_type = 2 ");
        if (StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)) {
            reportSb.append("   AND receive_time BETWEEN" + startTime + " AND " + endTime + "");
        }
        if (StrUtil.isNotBlank(userIdsql)) {
            reportSb.append("   AND user_id IN (" + userIdsql + ") ");
        }
        reportSb.append(" \t\t) c ");
        reportSql = reportSb.toString();
        log.info(reportSql);
        Query countQuery = entityManager.createNativeQuery(reportSql);
        List<Object> resultList = countQuery.getResultList();
        if (CollUtil.isEmpty(resultList)) {
            return new HashMap<>();
        }
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = new HashMap<>();
        Object[] obj = (Object[]) result;
        for (int i = 0; i < list.size(); i++) {
            String field = list.get(i);
            Object value = obj[i];
            map.put(field, value);
        }
        return map;
    }

//    public Map<String, Object> findVipTotalMapNotAccount(String startTime, String endTime, String levelArray, Long userId) {
//        startTime = "'"  + startTime +  "'";
//        endTime = "'" +  endTime  + "'";
//        String sql = "";
//        List<String> list = VIP_TOTAL_REPORT_VO_FIELD_LIST;
//        StringBuffer stringBuffer = new StringBuffer();
//
//        stringBuffer.append("    select\n" +
//                "    ifnull(main_t.num,0) num,\n" +
//                "    ifnull(main_t.bet_amount,0) bet_amount ,\n" +
//                "    ifnull(main_t.validbet,0) totalValidBet ,\n" +
//                "    ifnull(main_t.win_loss,0) totalWinLoss, \n" +
//                "    ifnull(td.todayAward,0) totalTodayAward, \n" +
//                "    ifnull(rs.riseAward,0) totalRiseWinLoss   \n" +
//                "\t from  (\n" +
//                "\t\t\tSELECT \n" +
//                "    account ,\n" +
//                "    id\n" +
//                "\t\tFROM\n" +
//                "\t\t\t`user` \n" +
//                "\t\tWHERE 1 = 1  ");
//        if (StrUtil.isNotBlank(levelArray)) {
//            stringBuffer.append(" and   `level` IN ("+ levelArray +") ");
//        }
//        if (ObjectUtil.isNotNull(userId)) {
//            stringBuffer.append(" and   `id` = "+userId+" ");
//        }
//        stringBuffer.append(" ) u ");
//        stringBuffer.append("  left join (\n" +
//                "        select user_id ,\n" +
//                "        SUM(betting_number) num,\n" +
//                "    sum(bet_amount) bet_amount,\n" +
//                "    sum(valid_amount) validbet ,\n" +
//                "    sum(win_loss) win_loss\n" +
//                "    from proxy_game_record_report gr\n" +
//                "    where  1=1   ");
//        if (StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)) {
//            stringBuffer.append("   and order_times  BETWEEN" +  startTime  + " AND "+   endTime  + "");
//        }
//        stringBuffer.append("  group by user_id  ) main_t on u.id = main_t.user_id ");
//        stringBuffer.append("       LEFT JOIN (\n" +
//                "                SELECT\n" +
//                "                    user_id,\n" +
//                "                    SUM(amount) AS todayAward\n" +
//                "                FROM\n" +
//                "                    award_receive_record\n" +
//                "                WHERE\n" +
//                "                 award_type = 1  ");
//        if (StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)) {
//            stringBuffer.append("  AND create_time  BETWEEN" +  startTime  + " AND "+   endTime  + "");
//        }
//        stringBuffer.append(" GROUP BY   user_id  ) td ON u.id = td.user_id ");
//        stringBuffer.append("   LEFT JOIN (\n" +
//                "                SELECT\n" +
//                "                    user_id,\n" +
//                "                    SUM(amount) AS riseAward\n" +
//                "                FROM\n" +
//                "                    award_receive_record\n" +
//                "                WHERE\n" +
//                "                 award_type = 2 ");
//
//        if (StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)) {
//            stringBuffer.append("   AND receive_time BETWEEN" +  startTime  + " AND "+   endTime  + "");
//        }
//        stringBuffer.append(" GROUP BY  user_id ) rs ON u.id = rs.user_id");
//        sql = stringBuffer.toString();
//        log.info(sql);
//        Query countQuery = entityManager.createNativeQuery(sql);
//        List<Object> resultList = countQuery.getResultList();
//        if(CollUtil.isEmpty(resultList)){
//            return  new HashMap<>();
//        }
//        Object result = countQuery.getSingleResult();
//        Map<String, Object> map = new HashMap<>();
//        Object[] obj = (Object[]) result;
//        for (int i = 0; i < list.size(); i++) {
//            String field = list.get(i);
//            Object value = obj[i];
//            map.put(field, value);
//        }
//        return map;
//    }


    private static final List<String> VIP_REPORT_VO_FIELD_LIST = Arrays.asList("account", "id", "level", "createTime", "userId", "num",
            "betAmount", "validBet", "winLoss", "todayAward", "riseAward");

    private static final List<String> VIP_TOTAL_REPORT_VO_FIELD_LIST = Arrays.asList(
            "totalValidBet", "totalWinLoss", "totalTodayAward", "totalRiseAward");


    private List<Map<String, Object>> parsePersonReportMapList(List<Object> resultList, List<String> listString) {
        List<Map<String, Object>> list = null;
        if (resultList != null && resultList.size() > CommonConst.NUMBER_0) {
            list = new LinkedList<>();

            for (Object result : resultList) {
                Map<String, Object> map = new HashMap<>();
                Object[] obj = (Object[]) result;
                for (int i = 0; i < listString.size(); i++) {
                    String field = listString.get(i);
                    Object value = obj[i];
                    map.put(field, value);
                }
                list.add(map);
            }
        }
        return list;
    }

}