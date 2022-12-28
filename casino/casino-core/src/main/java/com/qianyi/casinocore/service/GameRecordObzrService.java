package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.GameRecordDG;
import com.qianyi.casinocore.model.GameRecordObty;
import com.qianyi.casinocore.model.GameRecordObzr;
import com.qianyi.casinocore.repository.GameRecordObtyRepository;
import com.qianyi.casinocore.repository.GameRecordObzrRepository;
import com.qianyi.casinocore.repository.GameRecordObzrTimeRepository;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
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
public class GameRecordObzrService {
    @Autowired
    private GameRecordObzrRepository gameRecordObzrRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime) {
        List<Map<String, Object>> orderAmountVoList = gameRecordObzrRepository.getStatisticsResult(startTime, endTime);
        String json = JSON.toJSONString(orderAmountVoList);
        return JSON.parseArray(json, CompanyOrderAmountVo.class);

    }

    public GameRecordObzr findByBetOrderNo(String orderNo) {
        return gameRecordObzrRepository.findByOrderNo(orderNo);
    }

    public int countByIdLessThanEqualAndUserId(Date createTime, Long userId) {
        return gameRecordObzrRepository.countByIdLessThanEqualAndUserId(createTime, userId);
    }


    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordObzrRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordObzrRepository.updateWashCodeStatus(id, washCodeStatus);
    }

    public void updateLevelWaterStatus(Long id, Integer levelWater) {
        gameRecordObzrRepository.updateLevelWaterStatus(id, levelWater);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordObzrRepository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        gameRecordObzrRepository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public void updateProfitStatus(Long id, Integer shareProfitStatus) {
        gameRecordObzrRepository.updateProfitStatus(id, shareProfitStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordObzrRepository.updateExtractStatus(id, extractStatus);
    }

    public List<Map<String, Object>> queryGameRecords(Long id, Integer num) {
        return gameRecordObzrRepository.queryGameRecords(id, num);
    }

    public GameRecordObzr findGameRecordById(Long gameId) {
        return gameRecordObzrRepository.findById(gameId).orElse(null);
    }

    public GameRecordObzr save(GameRecordObzr gameRecord) {
        return gameRecordObzrRepository.save(gameRecord);
    }

    public GameRecordObzr findRecordRecordSum(GameRecordObzr gameRecordObzr, String startBetTime, String endBetTime, String startSetTime, String endSetTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordObzr> query = cb.createQuery(GameRecordObzr.class);
        Root<GameRecordObzr> root = query.from(GameRecordObzr.class);

        query.multiselect(
                cb.sum(root.get("payoutAmount").as(BigDecimal.class)).alias("payoutAmount"),
                cb.sum(root.get("netAmount").as(BigDecimal.class)).alias("netAmount"),
                cb.sum(root.get("betAmount").as(BigDecimal.class)).alias("betAmount"),
                cb.sum(root.get("validBetAmount").as(BigDecimal.class)).alias("validBetAmount")
        );

        List<Predicate> list = new ArrayList();
        if (!CommonUtil.checkNull(gameRecordObzr.getPlayerName())) {
            list.add(cb.equal(root.get("playerName").as(String.class), gameRecordObzr.getPlayerName()));
        }
        if (!CommonUtil.checkNull(gameRecordObzr.getOrderNo())) {
            list.add(cb.equal(root.get("orderNo").as(String.class), gameRecordObzr.getOrderNo()));
        }
        if (gameRecordObzr.getUserId() != null) {
            list.add(cb.equal(root.get("userId").as(Long.class), gameRecordObzr.getUserId()));
        }
        if (gameRecordObzr.getFirstProxy() != null) {
            list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordObzr.getFirstProxy()));
        }
        if (gameRecordObzr.getSecondProxy() != null) {
            list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordObzr.getSecondProxy()));
        }
        if (gameRecordObzr.getThirdProxy() != null) {
            list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordObzr.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
            list.add(
                    cb.between(root.get("betStrTime").as(String.class), startBetTime, endBetTime)
            );
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            list.add(
                    cb.between(root.get("settleStrTime").as(String.class), startSetTime, endSetTime)
            );
        }
        query
                .where(list.toArray(new Predicate[list.size()]));
        GameRecordObzr singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public Page<GameRecordObzr> findGameRecordPage(GameRecordObzr gameRecordObzr, Pageable pageable, String startBetTime, String endBetTime, String startSetTime, String endSetTime) {
        Specification<GameRecordObzr> condition = getCondition(gameRecordObzr, startBetTime, endBetTime, startSetTime, endSetTime);

        return gameRecordObzrRepository.findAll(condition, pageable);
    }

    private Specification<GameRecordObzr> getCondition(GameRecordObzr gameRecordObzr, String startTime, String endTime, String startSetTime, String endSetTime) {
        Specification<GameRecordObzr> specification = (root, criteriaQuery, cb) -> {
            Predicate predicate;
            List<Predicate> list = new ArrayList<Predicate>();
            if (!CommonUtil.checkNull(gameRecordObzr.getPlayerName())) {
                list.add(cb.equal(root.get("playerName").as(String.class), gameRecordObzr.getPlayerName()));
            }
            if (!CommonUtil.checkNull(gameRecordObzr.getOrderNo())) {
                list.add(cb.equal(root.get("orderNo").as(String.class), gameRecordObzr.getOrderNo()));
            }
            if (gameRecordObzr.getUserId() != null) {
                list.add(cb.equal(root.get("userId").as(Long.class), gameRecordObzr.getUserId()));
            }
            if (gameRecordObzr.getFirstProxy() != null) {
                list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordObzr.getFirstProxy()));
            }
            if (gameRecordObzr.getSecondProxy() != null) {
                list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordObzr.getSecondProxy()));
            }
            if (gameRecordObzr.getThirdProxy() != null) {
                list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordObzr.getThirdProxy()));
            }
            if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                list.add(
                        cb.between(root.get("betStrTime").as(String.class), startTime, endTime)
                );
            }
            if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
                list.add(
                        cb.between(root.get("settleStrTime").as(String.class), startSetTime, endSetTime)
                );
            }
            predicate = cb.and(list.toArray(new Predicate[list.size()]));
            return predicate;
        };
        return specification;
    }

    public List<GameRecordObzr> findGameRecord(GameRecordObzr gameRecord, String startTime, String endTime) {
        Specification<GameRecordObzr> condition = getConditionGameRecord(gameRecord, startTime, endTime);

        return gameRecordObzrRepository.findAll(condition);
    }

    private Specification<GameRecordObzr> getConditionGameRecord(GameRecordObzr gameRecord, String startTime, String endTime) {
        Specification<GameRecordObzr> specification = (root, criteriaQuery, cb) -> {
            Predicate predicate;
            List<Predicate> list = new ArrayList<Predicate>();
            if (gameRecord.getGameRecordStatus() != null) {
                list.add(cb.equal(root.get("gameRecordStatus").as(Integer.class), gameRecord.getGameRecordStatus()));
            }

            if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                list.add(
                        cb.between(root.get("createTime").as(String.class), startTime, endTime)
                );
            }
            predicate = cb.and(list.toArray(new Predicate[list.size()]));
            return predicate;
        };
        return specification;
    }

}