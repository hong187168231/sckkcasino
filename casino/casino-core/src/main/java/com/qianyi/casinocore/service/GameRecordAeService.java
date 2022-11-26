package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordAe;
import com.qianyi.casinocore.repository.GameRecordAeRepository;
import com.qianyi.casinocore.vo.GameRecordAeSummaryVo;
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
public class GameRecordAeService {

    @Autowired
    private GameRecordAeRepository gameRecordAeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public GameRecordAe findByPlatformAndPlatformTxId(String platform,String platformTxId){
        return gameRecordAeRepository.findByPlatformAndPlatformTxId(platform,platformTxId);
    }
    public GameRecordAe save(GameRecordAe gameRecordAe){
        return gameRecordAeRepository.save(gameRecordAe);
    }

    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordAeRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordAeRepository.updateWashCodeStatus(id, washCodeStatus);
    }

    public void updateLevelWaterStatus(Long id,Integer levelWater){
        gameRecordAeRepository.updateLevelWaterStatus(id,levelWater);
    }


    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordAeRepository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        gameRecordAeRepository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public void updateProfitStatus(Long id, Integer shareProfitStatus) {
        gameRecordAeRepository.updateProfitStatus(id, shareProfitStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordAeRepository.updateExtractStatus(id,extractStatus);
    }


    public Page<GameRecordAe> findGameRecordAePage(GameRecordAe gameRecordAe, Pageable pageable, String startBetTime, String endBetTime,
        String startSetTime, String endSetTime)  {
        Specification<GameRecordAe> condition = getCondition(gameRecordAe,startBetTime,endBetTime,startSetTime ,endSetTime);
        return gameRecordAeRepository.findAll(condition, pageable);
    }

    private Specification<GameRecordAe> getCondition(GameRecordAe gameRecordAe, String startBetTime, String endBetTime, String startSetTime, String endSetTime) {
        Specification<GameRecordAe> specification = new Specification<GameRecordAe>() {
            @Override
            public Predicate toPredicate(Root<GameRecordAe> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                //                if (!CommonUtil.checkNull(GameRecordAe.getUserName())) {
                //                    list.add(cb.equal(root.get("userName").as(String.class), GameRecordAe.getUserName()));
                //                }
                //                if (!CommonUtil.checkNull(GameRecordAe.getOrderNo())) {
                //                    list.add(cb.equal(root.get("orderNo").as(String.class), GameRecordAe.getOrderNo()));
                //                }
                if (!CommonUtil.checkNull(gameRecordAe.getGameType())) {
                    list.add(cb.equal(root.get("gameType").as(String.class), gameRecordAe.getGameType()));
                }
                if (!CommonUtil.checkNull(gameRecordAe.getPlatform())) {
                    list.add(cb.equal(root.get("platform").as(String.class), gameRecordAe.getPlatform()));
                }
                if (!CommonUtil.checkNull(gameRecordAe.getPlatformTxId())) {
                    list.add(cb.equal(root.get("platformTxId").as(String.class), gameRecordAe.getPlatformTxId()));
                }
                //                if (GameRecordAe.getOutcome() != null) {
                //                    list.add(cb.equal(root.get("outcome").as(Integer.class), GameRecordAe.getOutcome()));
                //                }
                if (gameRecordAe.getTxStatus() != null) {
                    list.add(cb.equal(root.get("txStatus").as(Integer.class), gameRecordAe.getTxStatus()));
                }
                if (gameRecordAe.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecordAe.getUserId()));
                }
                //                if (!CommonUtil.checkNull(gameRecordAe.getUserId())) {
                //                    list.add(cb.equal(root.get("userId").as(String.class), gameRecordAe.getUserId()));
                //                }
                if (gameRecordAe.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordAe.getFirstProxy()));
                }
                if (gameRecordAe.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordAe.getSecondProxy()));
                }
                if (gameRecordAe.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordAe.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
                    list.add(
                        cb.between(root.get("betTime").as(String.class), startBetTime, endBetTime)
                    );
                }
                if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
                    list.add(
                        cb.between(root.get("updateTimeStr").as(String.class), startSetTime, endSetTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public  GameRecordAe  findRecordRecordSum(GameRecordAe gameRecordAe,String startBetTime,String endBetTime, String startSetTime, String endSetTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordAe> query = cb.createQuery(GameRecordAe.class);
        Root<GameRecordAe> root = query.from(GameRecordAe.class);

        query.multiselect(
            cb.sum(root.get("betAmount").as(BigDecimal.class)).alias("betAmount"),
            cb.sum(root.get("realBetAmount").as(BigDecimal.class)).alias("realBetAmount"),
            cb.sum(root.get("winAmount").as(BigDecimal.class)).alias("winAmount"),
            cb.sum(root.get("turnover").as(BigDecimal.class)).alias("turnover")
        );

        List<Predicate> list = new ArrayList();
        if (!CommonUtil.checkNull(gameRecordAe.getGameType())) {
            list.add(cb.equal(root.get("gameType").as(String.class), gameRecordAe.getGameType()));
        }
        if (!CommonUtil.checkNull(gameRecordAe.getPlatform())) {
            list.add(cb.equal(root.get("platform").as(String.class), gameRecordAe.getPlatform()));
        }
        if (!CommonUtil.checkNull(gameRecordAe.getPlatformTxId())) {
            list.add(cb.equal(root.get("platformTxId").as(String.class), gameRecordAe.getPlatformTxId()));
        }
        //                if (GameRecordAe.getOutcome() != null) {
        //                    list.add(cb.equal(root.get("outcome").as(Integer.class), GameRecordAe.getOutcome()));
        //                }
        if (gameRecordAe.getTxStatus() != null) {
            list.add(cb.equal(root.get("txStatus").as(Integer.class), gameRecordAe.getTxStatus()));
        }
        if (gameRecordAe.getUserId() != null) {
            list.add(cb.equal(root.get("userId").as(Long.class), gameRecordAe.getUserId()));
        }
        //        if (!CommonUtil.checkNull(gameRecordAe.getUserId())) {
        //            list.add(cb.equal(root.get("userId").as(String.class), gameRecordAe.getUserId()));
        //        }
        if (gameRecordAe.getFirstProxy() != null) {
            list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordAe.getFirstProxy()));
        }
        if (gameRecordAe.getSecondProxy() != null) {
            list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordAe.getSecondProxy()));
        }
        if (gameRecordAe.getThirdProxy() != null) {
            list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordAe.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
            list.add(
                cb.between(root.get("betTime").as(String.class), startBetTime, endBetTime)
            );
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            list.add(
                cb.between(root.get("updateTimeStr").as(String.class), startSetTime, endSetTime)
            );
        }
        query
            .where(list.toArray(new Predicate[list.size()]));
        GameRecordAe singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public GameRecordAe findGameRecordById(Long gameRecordId) {
        return gameRecordAeRepository.findById(gameRecordId).orElse(null);
    }

    public int countByIdLessThanEqualAndUserId(Date createTime, Long userId) {
        return gameRecordAeRepository.countByIdLessThanEqualAndUserId(createTime,userId);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num,String platform){
        return gameRecordAeRepository.queryGameRecords(id,num,platform);
    }

    public List<Map<String,Object>> queryGameRecordsMerge(Long id,Integer num,String platform){
        return gameRecordAeRepository.queryGameRecordsMerge(id,num,platform);
    }

    public List<GameRecordAe> findGameRecordAe(GameRecordAe gameRecord,String startTime,String endTime)  {
        Specification<GameRecordAe> condition = getConditionGameRecord(gameRecord,startTime,endTime);
        return gameRecordAeRepository.findAll(condition);
    }

    private Specification<GameRecordAe> getConditionGameRecord(GameRecordAe gameRecordAe, String startTime, String endTime) {
        Specification<GameRecordAe> specification = new Specification<GameRecordAe>() {
            @Override
            public Predicate toPredicate(Root<GameRecordAe> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (gameRecordAe.getGameRecordStatus() != null) {
                    list.add(cb.equal(root.get("gameRecordStatus").as(Integer.class), gameRecordAe.getGameRecordStatus()));
                }
                if (gameRecordAe.getTxStatus() != null) {
                    list.add(cb.equal(root.get("txStatus").as(Integer.class), gameRecordAe.getTxStatus()));
                }
                if (!CommonUtil.checkNull(gameRecordAe.getPlatform())) {
                    list.add(cb.equal(root.get("platform").as(String.class), gameRecordAe.getPlatform()));
                }
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

    public Map<String,Object> findSumByPlatformAndTime(String platform, String startTime, String endTime) {
        return gameRecordAeRepository.findSumByPlatformAndTime(platform,startTime,endTime);
    }
}
