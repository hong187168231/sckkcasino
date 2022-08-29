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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GameRecordAeService {

    @Autowired
    private GameRecordAeRepository gameRecordAeRepository;

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

    public Page<GameRecordAe> findGameRecordAePage(GameRecordAe gameRecordAe, Pageable pageable, String startBetTime, String endBetTime)  {
        Specification<GameRecordAe> condition = getCondition(gameRecordAe,startBetTime,endBetTime);
        return gameRecordAeRepository.findAll(condition, pageable);
    }

    private Specification<GameRecordAe> getCondition(GameRecordAe gameRecordAe, String startTime, String endTime) {
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
//                if (GameRecordAe.getOutcome() != null) {
//                    list.add(cb.equal(root.get("outcome").as(Integer.class), GameRecordAe.getOutcome()));
//                }
                if (gameRecordAe.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecordAe.getUserId()));
                }
                if (gameRecordAe.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordAe.getFirstProxy()));
                }
                if (gameRecordAe.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordAe.getSecondProxy()));
                }
                if (gameRecordAe.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordAe.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                        cb.between(root.get("betStrTime").as(String.class), startTime, endTime)
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

    public GameRecordAe findGameRecordById(Long gameRecordId) {
        return gameRecordAeRepository.findById(gameRecordId).orElse(null);
    }

    public int countByIdLessThanEqualAndUserId(Date createTime, Long userId) {
        return gameRecordAeRepository.countByIdLessThanEqualAndUserId(createTime,userId);
    }
}
