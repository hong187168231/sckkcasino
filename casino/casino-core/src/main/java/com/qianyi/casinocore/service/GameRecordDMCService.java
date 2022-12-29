package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordDMC;
import com.qianyi.casinocore.repository.GameRecordDMCRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GameRecordDMCService {

    @Autowired
    private GameRecordDMCRepository gameRecordDMCRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public GameRecordDMC findByEnterpriseIdAndBetOrderNo(String enterpriseId, String betOrderNo) {

        return gameRecordDMCRepository.findByEnterpriseIdAndBetOrderNo(enterpriseId, betOrderNo);
    }

    public GameRecordDMC save(GameRecordDMC gameRecord) {
        return gameRecordDMCRepository.save(gameRecord);
    }

    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordDMCRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordDMCRepository.updateWashCodeStatus(id, washCodeStatus);
    }
    public void updateLevelWaterStatus(Long id, Integer washCodeStatus) {
        gameRecordDMCRepository.updateLevelWaterStatus(id, washCodeStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordDMCRepository.updateExtractStatus(id,extractStatus);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordDMCRepository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        gameRecordDMCRepository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public Map<String,Object> findSumByPlatformAndTime(String platform, String startTime, String endTime) {
        return gameRecordDMCRepository.findSumByPlatformAndTime(platform,startTime,endTime);
    }

    public int countByIdLessThanEqualAndUserId(Date createTime, Long userId) {
        return gameRecordDMCRepository.countByIdLessThanEqualAndUserId(createTime,userId);
    }

    public GameRecordDMC findGameRecordById(Long gameRecordId) {
        return gameRecordDMCRepository.findById(gameRecordId).orElse(null);
    }

    public List<Map<String,Object>> queryGameRecords(Long id, Integer num, String platform){
        return gameRecordDMCRepository.queryGameRecords(id,num,platform);
    }

    public List<GameRecordDMC> findGameRecordDMC(GameRecordDMC gameRecord,String startTime,String endTime)  {
        Specification<GameRecordDMC> condition = getConditionGameRecord(gameRecord,startTime,endTime);
        return gameRecordDMCRepository.findAll(condition);
    }

    private Specification<GameRecordDMC> getConditionGameRecord(GameRecordDMC GameRecordDMC, String startTime, String endTime) {
        Specification<GameRecordDMC> specification = new Specification<GameRecordDMC>() {
            @Override
            public Predicate toPredicate(Root<GameRecordDMC> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (GameRecordDMC.getGameRecordStatus() != null) {
                    list.add(cb.equal(root.get("gameRecordStatus").as(Integer.class), GameRecordDMC.getGameRecordStatus()));
                }
//                if (gameRecordAe.getTxStatus() != null) {
//                    list.add(cb.equal(root.get("txStatus").as(Integer.class), gameRecordAe.getTxStatus()));
//                }
//                if (!CommonUtil.checkNull(gameRecordAe.getPlatform())) {
//                    list.add(cb.equal(root.get("platform").as(String.class), gameRecordAe.getPlatform()));
//                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                            cb.between(root.get("createTime").as(String.class), startTime, endTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public Page<GameRecordDMC> findGameRecordDMCPage(GameRecordDMC gameRecord, Pageable pageable, String startBetTime, String endBetTime, String startSetTime, String endSetTime) {
        Specification<GameRecordDMC> condition = getCondition(gameRecord,startBetTime,endBetTime,startSetTime,endSetTime);
        return gameRecordDMCRepository.findAll(condition, pageable);
    }

    private Specification<GameRecordDMC> getCondition(GameRecordDMC GameRecordDMC,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<GameRecordDMC> specification = new Specification<GameRecordDMC>() {
            @Override
            public Predicate toPredicate(Root<GameRecordDMC> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(GameRecordDMC.getBetOrderNo())) {
                    list.add(cb.equal(root.get("betOrderNo").as(String.class), GameRecordDMC.getBetOrderNo()));
                }
//                if (!CommonUtil.checkNull(GameRecordDMC.getAccount())) {
//                    list.add(cb.equal(root.get("account").as(String.class), GameRecordDMC.getAccount()));
//                }
                if (!CommonUtil.checkNull(GameRecordDMC.getUserName())) {
                    list.add(cb.equal(root.get("userName").as(String.class), GameRecordDMC.getUserName()));
                }
//                if (gameRecord.getGid() != null) {
//                    list.add(cb.equal(root.get("gid").as(Integer.class), gameRecord.getGid()));
//                }
                if (GameRecordDMC.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), GameRecordDMC.getUserId()));
                }
                if (GameRecordDMC.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), GameRecordDMC.getFirstProxy()));
                }
                if (GameRecordDMC.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), GameRecordDMC.getSecondProxy()));
                }
                if (GameRecordDMC.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), GameRecordDMC.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
                    list.add(
                            cb.between(root.get("betTime").as(String.class), startBetTime, endBetTime)
                    );
                }
                if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
                    list.add(
                            cb.between(root.get("betTime").as(String.class), startSetTime, endSetTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public  GameRecordDMC  findGameRecordDMCSum(GameRecordDMC GameRecordDMC,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordDMC> query = cb.createQuery(GameRecordDMC.class);
        Root<GameRecordDMC> root = query.from(GameRecordDMC.class);

        query.multiselect(
                cb.sum(root.get("betMoney").as(BigDecimal.class)).alias("betMoney"),
                cb.sum(root.get("backWaterMoney").as(BigDecimal.class)).alias("backWaterMoney"),
                cb.sum(root.get("realMoney").as(BigDecimal.class)).alias("realMoney"),
                cb.sum(root.get("winMoney").as(BigDecimal.class)).alias("winMoney")
        );

        List<Predicate> list = new ArrayList();

        if (!CommonUtil.checkNull(GameRecordDMC.getBetOrderNo())) {
            list.add(cb.equal(root.get("betOrderNo").as(String.class), GameRecordDMC.getBetOrderNo()));
        }
//        if (!CommonUtil.checkNull(GameRecordDMC.getAccount())) {
//            list.add(cb.equal(root.get("account").as(String.class), GameRecordDMC.getAccount()));
//        }
        if (!CommonUtil.checkNull(GameRecordDMC.getUserName())) {
            list.add(cb.equal(root.get("userName").as(String.class), GameRecordDMC.getUserName()));
        }
        if (GameRecordDMC.getUserId() != null) {
            list.add(cb.equal(root.get("userId").as(Long.class), GameRecordDMC.getUserId()));
        }
        if (GameRecordDMC.getFirstProxy() != null) {
            list.add(cb.equal(root.get("firstProxy").as(Long.class), GameRecordDMC.getFirstProxy()));
        }
        if (GameRecordDMC.getSecondProxy() != null) {
            list.add(cb.equal(root.get("secondProxy").as(Long.class), GameRecordDMC.getSecondProxy()));
        }
        if (GameRecordDMC.getThirdProxy() != null) {
            list.add(cb.equal(root.get("thirdProxy").as(Long.class), GameRecordDMC.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
            list.add(
                    cb.between(root.get("betTime").as(String.class), startBetTime, endBetTime)
            );
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            list.add(
                    cb.between(root.get("betTime").as(String.class), startSetTime, endSetTime)
            );
        }
        query
                .where(list.toArray(new Predicate[list.size()]));
        GameRecordDMC singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }
}
