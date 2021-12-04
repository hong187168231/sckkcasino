package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.ConsumerError;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.GameRecordRepository;
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
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GameRecordService {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @PersistenceContext
    private EntityManager entityManager;



    public Page<GameRecord> findGameRecordPage(GameRecord gameRecord, Pageable pageable,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<GameRecord> condition = getCondition(gameRecord,startBetTime,endBetTime,startSetTime,endSetTime);
        return gameRecordRepository.findAll(condition, pageable);
    }

    public List<GameRecord> findGameRecords(GameRecord gameRecord,String startTime,String endTime) {
        Specification<GameRecord> condition = getCondition(gameRecord,startTime,endTime);
        return gameRecordRepository.findAll(condition);
    }

    public  GameRecord  findRecordRecordSum(GameRecord game) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecord> query = builder.createQuery(GameRecord.class);
        Root<GameRecord> root = query.from(GameRecord.class);

        query.multiselect(
                builder.sum(root.get("bet").as(BigDecimal.class)).alias("bet"),
                builder.sum(root.get("validbet").as(BigDecimal.class)).alias("validbet"),
                builder.sum(root.get("water").as(BigDecimal.class)).alias("water"),
                builder.sum(root.get("waterbet").as(BigDecimal.class)).alias("waterbet"),
                builder.sum(root.get("winLoss").as(BigDecimal.class)).alias("winLoss")
        );

        List<Predicate> predicates = new ArrayList();

        if (!CommonUtil.checkNull(game.getBetId())) {
            predicates.add(
                    builder.equal(root.get("betId").as(String.class), game.getBetId())
            );
        }
        if (!CommonUtil.checkNull(game.getUser())) {
            predicates.add(
                    builder.equal(root.get("user").as(String.class), game.getUser())
            );
        }
        if (!CommonUtil.checkNull(game.getGname())) {
            predicates.add(
                    builder.equal(root.get("gname").as(String.class), game.getGname())
            );
        }
        if (game.getGid() != null) {
            predicates.add(
                    builder.equal(root.get("gid").as(Integer.class), game.getGid())
            );
        }
        if (game.getFirstProxy() != null) {
            predicates.add(
                    builder.equal(root.get("firstProxy").as(Long.class), game.getFirstProxy())
            );
        }
        if (game.getSecondProxy() != null) {
            predicates.add(
                    builder.equal(root.get("secondProxy").as(Long.class), game.getSecondProxy())
            );
        }
        if (game.getThirdProxy() != null) {
            predicates.add(
                    builder.equal(root.get("thirdProxy").as(Long.class), game.getThirdProxy())
            );
        }
        query
                .where(predicates.toArray(new Predicate[predicates.size()]));
//                .groupBy(root.get("conversionStepCode"))
//                .orderBy(builder.desc(root.get("contactUserNums")));
        GameRecord singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public GameRecord save(GameRecord gameRecord) {
        return gameRecordRepository.save(gameRecord);
    }

    public GameRecord findGameRecordById(Long gameId){return gameRecordRepository.findById(gameId).orElse(null);}


    public int countByIdLessThanEqualAndUserId(Long gameId,Long UserId){
        return gameRecordRepository.countByIdLessThanEqualAndUserId(gameId,UserId);
    }


    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<GameRecord> getCondition(GameRecord gameRecord,String startTime,String endTime) {
        Specification<GameRecord> specification = new Specification<GameRecord>() {
            @Override
            public Predicate toPredicate(Root<GameRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(gameRecord.getUser())) {
                    list.add(cb.equal(root.get("user").as(String.class), gameRecord.getUser()));
                }
                if (!CommonUtil.checkNull(gameRecord.getBetId())) {
                    list.add(cb.equal(root.get("betId").as(String.class), gameRecord.getBetId()));
                }
                if (!CommonUtil.checkNull(gameRecord.getGname())) {
                    list.add(cb.equal(root.get("gname").as(String.class), gameRecord.getGname()));
                }
                if (gameRecord.getGid() != null) {
                    list.add(cb.equal(root.get("gid").as(Integer.class), gameRecord.getGid()));
                }
                if (gameRecord.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecord.getUserId()));
                }
                if (gameRecord.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecord.getFirstProxy()));
                }
                if (gameRecord.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecord.getSecondProxy()));
                }
                if (gameRecord.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecord.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                            cb.between(root.get("betTime").as(String.class), startTime, endTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<GameRecord> getCondition(GameRecord gameRecord,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<GameRecord> specification = new Specification<GameRecord>() {
            @Override
            public Predicate toPredicate(Root<GameRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(gameRecord.getUser())) {
                    list.add(cb.equal(root.get("user").as(String.class), gameRecord.getUser()));
                }
                if (!CommonUtil.checkNull(gameRecord.getBetId())) {
                    list.add(cb.equal(root.get("betId").as(String.class), gameRecord.getBetId()));
                }
                if (!CommonUtil.checkNull(gameRecord.getGname())) {
                    list.add(cb.equal(root.get("gname").as(String.class), gameRecord.getGname()));
                }
                if (gameRecord.getGid() != null) {
                    list.add(cb.equal(root.get("gid").as(Integer.class), gameRecord.getGid()));
                }
                if (gameRecord.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecord.getUserId()));
                }
                if (gameRecord.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecord.getFirstProxy()));
                }
                if (gameRecord.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecord.getSecondProxy()));
                }
                if (gameRecord.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecord.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
                    list.add(
                            cb.between(root.get("betTime").as(String.class), startBetTime, endBetTime)
                    );
                }
                if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
                    list.add(
                            cb.between(root.get("settime").as(String.class), startSetTime, endSetTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
    public List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime){
        List<Map<String,Object>> orderAmountVoList = gameRecordRepository.getStatisticsResult(startTime,endTime);
        String json = JSON.toJSONString(orderAmountVoList);
        return JSON.parseArray(json,CompanyOrderAmountVo.class);
    }

    public List<GameRecord> findGameRecordIdAll(List<Long> recordIdList) {
        return gameRecordRepository.findAllById(recordIdList);
    }
    public  GameRecord  findRecordRecordSum(Long userId,String startTime,String endTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecord> query = builder.createQuery(GameRecord.class);
        Root<GameRecord> root = query.from(GameRecord.class);

        query.multiselect(
                builder.sum(root.get("validbet").as(BigDecimal.class)).alias("validbet")
        );

        List<Predicate> predicates = new ArrayList();
        if (userId != null) {
            predicates.add(
                    builder.equal(root.get("userId").as(Long.class), userId)
            );
        }
        if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
            predicates.add(
                    builder.between(root.get("betTime").as(String.class), startTime, endTime)
            );
        }
        query
                .where(predicates.toArray(new Predicate[predicates.size()]));
//                .groupBy(root.get("conversionStepCode"))
//                .orderBy(builder.desc(root.get("contactUserNums")));
        GameRecord singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }
    public  GameRecord  findRecordRecordSum(String startTime,String endTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecord> query = builder.createQuery(GameRecord.class);
        Root<GameRecord> root = query.from(GameRecord.class);

        query.multiselect(
                builder.sum(root.get("bet").as(BigDecimal.class)).alias("bet"),
                builder.sum(root.get("winLoss").as(BigDecimal.class)).alias("winLoss")
        );

        List<Predicate> predicates = new ArrayList();
        if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
            predicates.add(
                    builder.between(root.get("betTime").as(String.class), startTime, endTime)
            );
        }
        query
                .where(predicates.toArray(new Predicate[predicates.size()]));
//                .groupBy(root.get("conversionStepCode"))
//                .orderBy(builder.desc(root.get("contactUserNums")));
        GameRecord singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public void updateCodeNumStatus(Long id,Integer codeNumStatus){
        gameRecordRepository.updateCodeNumStatus(id,codeNumStatus);
    }

    public void updateWashCodeStatus(Long id,Integer washCodeStatus){
        gameRecordRepository.updateWashCodeStatus(id,washCodeStatus);
    }

    public void updateProfitStatus(Long id,Integer washCodeStatus){
        gameRecordRepository.updateProfitStatus(id,washCodeStatus);
    }
    public List<GameRecord> findByCreateByAndIdGreaterThanEqualOrderByIdAsc(String createBy,Long id) {
        return gameRecordRepository.findByCreateByAndIdGreaterThanEqualOrderByIdAsc(createBy,id);
    }
}
