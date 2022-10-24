package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordVNC;
import com.qianyi.casinocore.repository.GameRecordVNCRepository;
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
import java.util.List;
import java.util.Map;

@Service
public class GameRecordVNCService {

    @Autowired
    private GameRecordVNCRepository gameRecordVNCRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public GameRecordVNC findByMerchantCodeAndBetOrder(String merchantCode, String betOrder) {

        return gameRecordVNCRepository.findByMerchantCodeAndBetOrder(merchantCode, betOrder);
    }

    public GameRecordVNC save(GameRecordVNC gameRecord) {
        return gameRecordVNCRepository.save(gameRecord);
    }

    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordVNCRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordVNCRepository.updateWashCodeStatus(id, washCodeStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordVNCRepository.updateExtractStatus(id,extractStatus);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordVNCRepository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        gameRecordVNCRepository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public Map<String,Object> findSumByPlatformAndTime(String platform, String startTime, String endTime) {
        return gameRecordVNCRepository.findSumByPlatformAndTime(platform,startTime,endTime);
    }

    public GameRecordVNC findGameRecordById(Long gameRecordId) {
        return gameRecordVNCRepository.findById(gameRecordId).orElse(null);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num,String platform){
        return gameRecordVNCRepository.queryGameRecords(id,num,platform);
    }

    public List<GameRecordVNC> findGameRecordVNC(GameRecordVNC gameRecord,String startTime,String endTime)  {
        Specification<GameRecordVNC> condition = getConditionGameRecord(gameRecord,startTime,endTime);
        return gameRecordVNCRepository.findAll(condition);
    }

    private Specification<GameRecordVNC> getConditionGameRecord(GameRecordVNC gameRecordVNC, String startTime, String endTime) {
        Specification<GameRecordVNC> specification = new Specification<GameRecordVNC>() {
            @Override
            public Predicate toPredicate(Root<GameRecordVNC> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (gameRecordVNC.getGameRecordStatus() != null) {
                    list.add(cb.equal(root.get("gameRecordStatus").as(Integer.class), gameRecordVNC.getGameRecordStatus()));
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

    public Page<GameRecordVNC> findGameRecordVNCPage(GameRecordVNC gameRecord, Pageable pageable,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<GameRecordVNC> condition = getCondition(gameRecord,startBetTime,endBetTime,startSetTime,endSetTime);
        return gameRecordVNCRepository.findAll(condition, pageable);
    }

    private Specification<GameRecordVNC> getCondition(GameRecordVNC gameRecordVNC,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<GameRecordVNC> specification = new Specification<GameRecordVNC>() {
            @Override
            public Predicate toPredicate(Root<GameRecordVNC> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(gameRecordVNC.getBetOrder())) {
                    list.add(cb.equal(root.get("betOrder").as(String.class), gameRecordVNC.getBetOrder()));
                }
                if (!CommonUtil.checkNull(gameRecordVNC.getAccount())) {
                    list.add(cb.equal(root.get("account").as(String.class), gameRecordVNC.getAccount()));
                }
                if (!CommonUtil.checkNull(gameRecordVNC.getUserName())) {
                    list.add(cb.equal(root.get("userName").as(String.class), gameRecordVNC.getUserName()));
                }
                //                if (gameRecord.getGid() != null) {
                //                    list.add(cb.equal(root.get("gid").as(Integer.class), gameRecord.getGid()));
                //                }
                if (gameRecordVNC.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecordVNC.getUserId()));
                }
                if (gameRecordVNC.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordVNC.getFirstProxy()));
                }
                if (gameRecordVNC.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordVNC.getSecondProxy()));
                }
                if (gameRecordVNC.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordVNC.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
                    list.add(
                        cb.between(root.get("betTime").as(String.class), startBetTime, endBetTime)
                    );
                }
                if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
                    list.add(
                        cb.between(root.get("settleTime").as(String.class), startSetTime, endSetTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public  GameRecordVNC  findGameRecordVNCSum(GameRecordVNC gameRecordVNC,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordVNC> query = cb.createQuery(GameRecordVNC.class);
        Root<GameRecordVNC> root = query.from(GameRecordVNC.class);

        query.multiselect(
            cb.sum(root.get("betMoney").as(BigDecimal.class)).alias("betMoney"),
            cb.sum(root.get("backWaterMoney").as(BigDecimal.class)).alias("backWaterMoney"),
            cb.sum(root.get("realMoney").as(BigDecimal.class)).alias("realMoney"),
            cb.sum(root.get("winMoney").as(BigDecimal.class)).alias("winMoney")
        );

        List<Predicate> list = new ArrayList();

        if (!CommonUtil.checkNull(gameRecordVNC.getBetOrder())) {
            list.add(cb.equal(root.get("betOrder").as(String.class), gameRecordVNC.getBetOrder()));
        }
        if (!CommonUtil.checkNull(gameRecordVNC.getAccount())) {
            list.add(cb.equal(root.get("account").as(String.class), gameRecordVNC.getAccount()));
        }
        if (!CommonUtil.checkNull(gameRecordVNC.getUserName())) {
            list.add(cb.equal(root.get("userName").as(String.class), gameRecordVNC.getUserName()));
        }
        if (gameRecordVNC.getUserId() != null) {
            list.add(cb.equal(root.get("userId").as(Long.class), gameRecordVNC.getUserId()));
        }
        if (gameRecordVNC.getFirstProxy() != null) {
            list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordVNC.getFirstProxy()));
        }
        if (gameRecordVNC.getSecondProxy() != null) {
            list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordVNC.getSecondProxy()));
        }
        if (gameRecordVNC.getThirdProxy() != null) {
            list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordVNC.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
            list.add(
                cb.between(root.get("betTime").as(String.class), startBetTime, endBetTime)
            );
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            list.add(
                cb.between(root.get("settleTime").as(String.class), startSetTime, endSetTime)
            );
        }
        query
            .where(list.toArray(new Predicate[list.size()]));
        GameRecordVNC singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }
}
