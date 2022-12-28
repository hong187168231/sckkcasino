package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordDG;
import com.qianyi.casinocore.repository.GameRecordDGRepository;
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
public class GameRecordDGService {

    @Autowired
    private GameRecordDGRepository gameRecordDGRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public GameRecordDG findByBetOrderNo(String betOrderNo) {

        return gameRecordDGRepository.findByBetOrderNo( betOrderNo);
    }

    public GameRecordDG save(GameRecordDG gameRecord) {
        return gameRecordDGRepository.save(gameRecord);
    }

    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordDGRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordDGRepository.updateWashCodeStatus(id, washCodeStatus);
    }
    public void updateLevelWaterStatus(Long id, Integer washCodeStatus) {
        gameRecordDGRepository.updateLevelWaterStatus(id, washCodeStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordDGRepository.updateExtractStatus(id,extractStatus);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordDGRepository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        gameRecordDGRepository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public Map<String,Object> findSumByPlatformAndTime(String startTime, String endTime) {
        return gameRecordDGRepository.findSumByPlatformAndTime(startTime,endTime);
    }

    public GameRecordDG findGameRecordById(Long gameRecordId) {
        return gameRecordDGRepository.findById(gameRecordId).orElse(null);
    }

    public int countByIdLessThanEqualAndUserId(Date createTime, Long userId) {
        return gameRecordDGRepository.countByIdLessThanEqualAndUserId(createTime,userId);
    }

    public List<Map<String,Object>> queryGameRecords(Long id, Integer num){
        return gameRecordDGRepository.queryGameRecords(id,num);
    }

    public List<GameRecordDG> findGameRecordDG(GameRecordDG gameRecord,String startTime,String endTime)  {
        Specification<GameRecordDG> condition = getConditionGameRecord(gameRecord,startTime,endTime);
        return gameRecordDGRepository.findAll(condition);
    }

    private Specification<GameRecordDG> getConditionGameRecord(GameRecordDG GameRecordDG, String startTime, String endTime) {
        Specification<GameRecordDG> specification = new Specification<GameRecordDG>() {
            @Override
            public Predicate toPredicate(Root<GameRecordDG> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (GameRecordDG.getGameRecordStatus() != null) {
                    list.add(cb.equal(root.get("gameRecordStatus").as(Integer.class), GameRecordDG.getGameRecordStatus()));
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

    public Page<GameRecordDG> findGameRecordDGPage(GameRecordDG gameRecord, Pageable pageable, String startBetTime, String endBetTime, String startSetTime, String endSetTime) {
        Specification<GameRecordDG> condition = getCondition(gameRecord,startBetTime,endBetTime,startSetTime,endSetTime);
        return gameRecordDGRepository.findAll(condition, pageable);
    }

    private Specification<GameRecordDG> getCondition(GameRecordDG GameRecordDG,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<GameRecordDG> specification = new Specification<GameRecordDG>() {
            @Override
            public Predicate toPredicate(Root<GameRecordDG> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(GameRecordDG.getBetOrderNo())) {
                    list.add(cb.equal(root.get("betOrderNo").as(String.class), GameRecordDG.getBetOrderNo()));
                }
//                if (!CommonUtil.checkNull(GameRecordDG.getAccount())) {
//                    list.add(cb.equal(root.get("account").as(String.class), GameRecordDG.getAccount()));
//                }
                if (!CommonUtil.checkNull(GameRecordDG.getUserName())) {
                    list.add(cb.equal(root.get("userName").as(String.class), GameRecordDG.getUserName()));
                }
//                if (gameRecord.getGid() != null) {
//                    list.add(cb.equal(root.get("gid").as(Integer.class), gameRecord.getGid()));
//                }
                if (GameRecordDG.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), GameRecordDG.getUserId()));
                }
                if (GameRecordDG.getUserAcct() != null) {
                    list.add(cb.equal(root.get("userAcct").as(String.class), GameRecordDG.getUserAcct()));
                }
                if (GameRecordDG.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), GameRecordDG.getFirstProxy()));
                }
                if (GameRecordDG.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), GameRecordDG.getSecondProxy()));
                }
                if (GameRecordDG.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), GameRecordDG.getThirdProxy()));
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

    public  GameRecordDG  findGameRecordDGSum(GameRecordDG GameRecordDG,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordDG> query = cb.createQuery(GameRecordDG.class);
        Root<GameRecordDG> root = query.from(GameRecordDG.class);

        query.multiselect(
                cb.sum(root.get("betPoints").as(BigDecimal.class)).alias("betPoints"),
                cb.sum(root.get("winOrLoss").as(BigDecimal.class)).alias("winOrLoss"),
                cb.sum(root.get("realMoney").as(BigDecimal.class)).alias("realMoney"),
                cb.sum(root.get("winMoney").as(BigDecimal.class)).alias("winMoney"),
                cb.sum(root.get("availableBet").as(BigDecimal.class)).alias("availableBet")

        );

        List<Predicate> list = new ArrayList();

        if (!CommonUtil.checkNull(GameRecordDG.getBetOrderNo())) {
            list.add(cb.equal(root.get("betOrderNo").as(String.class), GameRecordDG.getBetOrderNo()));
        }
//        if (!CommonUtil.checkNull(GameRecordDG.getAccount())) {
//            list.add(cb.equal(root.get("account").as(String.class), GameRecordDG.getAccount()));
//        }
        if (!CommonUtil.checkNull(GameRecordDG.getUserName())) {
            list.add(cb.equal(root.get("userName").as(String.class), GameRecordDG.getUserName()));
        }
        if (GameRecordDG.getUserAcct() != null) {
            list.add(cb.equal(root.get("userAcct").as(String.class), GameRecordDG.getUserAcct()));
        }
        if (GameRecordDG.getUserId() != null) {
            list.add(cb.equal(root.get("userId").as(Long.class), GameRecordDG.getUserId()));
        }
        if (GameRecordDG.getFirstProxy() != null) {
            list.add(cb.equal(root.get("firstProxy").as(Long.class), GameRecordDG.getFirstProxy()));
        }
        if (GameRecordDG.getSecondProxy() != null) {
            list.add(cb.equal(root.get("secondProxy").as(Long.class), GameRecordDG.getSecondProxy()));
        }
        if (GameRecordDG.getThirdProxy() != null) {
            list.add(cb.equal(root.get("thirdProxy").as(Long.class), GameRecordDG.getThirdProxy()));
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
        GameRecordDG singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }
}
