package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordVNC;
import com.qianyi.casinocore.model.RptBetInfoDetail;
import com.qianyi.casinocore.repository.RptBetInfoDetailRepository;
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
import java.util.Optional;

@Service
public class RptBetInfoDetailService {

    @Autowired
    private RptBetInfoDetailRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    public RptBetInfoDetail save(RptBetInfoDetail rptBetInfoDetail) {
        return repository.save(rptBetInfoDetail);
    }

    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        repository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        repository.updateWashCodeStatus(id, washCodeStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        repository.updateExtractStatus(id,extractStatus);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        repository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        repository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public Map<String,Object> findSumByPlatformAndTime(String platform, String startTime, String endTime) {
        return repository.findSumByPlatformAndTime(platform,startTime,endTime);
    }

    public RptBetInfoDetail findGameRecordById(Long gameRecordId) {
        return repository.findById(gameRecordId).orElse(null);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num,String platform){
        return repository.queryGameRecords(id,num,platform);
    }

    public List<RptBetInfoDetail> findRptBetInfoDetail(RptBetInfoDetail gameRecord,String startTime,String endTime)  {
        Specification<RptBetInfoDetail> condition = getConditionGameRecord(gameRecord,startTime,endTime);
        return repository.findAll(condition);
    }

    private Specification<RptBetInfoDetail> getConditionGameRecord(RptBetInfoDetail gameRecordVNC, String startTime, String endTime) {
        Specification<RptBetInfoDetail> specification = new Specification<RptBetInfoDetail>() {
            @Override
            public Predicate toPredicate(Root<RptBetInfoDetail> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (gameRecordVNC.getGameRecordStatus() != null) {
                    list.add(cb.equal(root.get("gameRecordStatus").as(Integer.class), gameRecordVNC.getGameRecordStatus()));
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

    public Page<RptBetInfoDetail> findRptBetInfoDetailPage(RptBetInfoDetail gameRecord, Pageable pageable,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<RptBetInfoDetail> condition = getCondition(gameRecord,startBetTime,endBetTime,startSetTime,endSetTime);
        return repository.findAll(condition, pageable);
    }

    private Specification<RptBetInfoDetail> getCondition(RptBetInfoDetail gameRecordVNC,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<RptBetInfoDetail> specification = new Specification<RptBetInfoDetail>() {
            @Override
            public Predicate toPredicate(Root<RptBetInfoDetail> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(gameRecordVNC.getBetOrder())) {
                    list.add(cb.equal(root.get("betOrder").as(String.class), gameRecordVNC.getBetOrder()));
                }
                if (!CommonUtil.checkNull(gameRecordVNC.getBetDetailOrder())) {
                    list.add(cb.equal(root.get("betDetailOrder").as(String.class), gameRecordVNC.getBetDetailOrder()));
                }
                //                if (!CommonUtil.checkNull(gameRecordVNC.getAccount())) {
                //                    list.add(cb.equal(root.get("account").as(String.class), gameRecordVNC.getAccount()));
                //                }
                if (!CommonUtil.checkNull(gameRecordVNC.getUserName())) {
                    list.add(cb.equal(root.get("userName").as(String.class), gameRecordVNC.getUserName()));
                }
                //                if (gameRecord.getGid() != null) {
                //                    list.add(cb.equal(root.get("gid").as(Integer.class), gameRecord.getGid()));
                //                }
                if (gameRecordVNC.getGamePlay() != null) {
                    list.add(cb.equal(root.get("gamePlay").as(Integer.class), gameRecordVNC.getGamePlay()));
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
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public  RptBetInfoDetail  findRptBetInfoDetailSum(RptBetInfoDetail gameRecordVNC,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RptBetInfoDetail> query = cb.createQuery(RptBetInfoDetail.class);
        Root<RptBetInfoDetail> root = query.from(RptBetInfoDetail.class);

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
        if (!CommonUtil.checkNull(gameRecordVNC.getBetDetailOrder())) {
            list.add(cb.equal(root.get("betDetailOrder").as(String.class), gameRecordVNC.getBetDetailOrder()));
        }
        //        if (!CommonUtil.checkNull(gameRecordVNC.getAccount())) {
        //            list.add(cb.equal(root.get("account").as(String.class), gameRecordVNC.getAccount()));
        //        }
        if (!CommonUtil.checkNull(gameRecordVNC.getUserName())) {
            list.add(cb.equal(root.get("userName").as(String.class), gameRecordVNC.getUserName()));
        }
        if (gameRecordVNC.getGamePlay() != null) {
            list.add(cb.equal(root.get("gamePlay").as(Integer.class), gameRecordVNC.getGamePlay()));
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
        RptBetInfoDetail singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public RptBetInfoDetail findByMerchantCodeAndBetDetailOrder(String merchantCode, String betDetailOrder) {
        Optional<RptBetInfoDetail> optional = repository.findOne(getCondition(merchantCode, betDetailOrder));
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }


    private Specification<RptBetInfoDetail> getCondition(String merchantCode, String betDetailOrder) {
        Specification<RptBetInfoDetail> specification = new Specification<RptBetInfoDetail>() {
            @Override
            public Predicate toPredicate(Root<RptBetInfoDetail> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(betDetailOrder)) {
                    list.add(cb.equal(root.get("betDetailOrder").as(String.class), betDetailOrder));
                }

                if (!CommonUtil.checkNull(merchantCode)) {
                    list.add(cb.equal(root.get("merchantCode").as(String.class), merchantCode));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public void updateRptBetInfoDetailGamePlay(){
        repository.updateRptBetInfoDetailGamePlay();
    }
}
