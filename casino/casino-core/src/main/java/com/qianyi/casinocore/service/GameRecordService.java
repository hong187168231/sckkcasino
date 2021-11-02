package com.qianyi.casinocore.service;

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

@Service
public class GameRecordService {

    @Autowired
    private GameRecordRepository gameRecordRepository;
    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;



    public Page<GameRecord> findGameRecordPage(GameRecord gameRecord, Pageable pageable) {
        Specification<GameRecord> condition = getCondition(gameRecord);
        return gameRecordRepository.findAll(condition, pageable);
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

    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<GameRecord> getCondition(GameRecord gameRecord) {
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
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime){
        return gameRecordRepository.getStatisticsResult(startTime,endTime);
    }

    public List<GameRecord> findGameRecordIdAll(List<Long> recordIdList) {
        return gameRecordRepository.findAllById(recordIdList);
    }
}
