package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ExtractPointsChange;
import com.qianyi.casinocore.repository.ExtractPointsChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 抽点记录
 *
 * @author lance
 * @since 2022 -02-22 19:27:40
 */
@Service
public class ExtractPointsChangeService {

    @Autowired
    private ExtractPointsChangeRepository repository;

    /**
     * 查询开始时间和结束时间之间的抽点记录
     *
     * @param startDate 入参释义
     * @param endDate   入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-22 19:27:41
     */
    public List<ExtractPointsChange> findBetween(Date startDate, Date endDate) {
        Specification<ExtractPointsChange> condition = (root, q, cb) -> {
            List<Predicate> list = new ArrayList<>();
            if (startDate != null) {
                list.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createTime").as(Date.class), startDate
                        )
                );
            }
            if (endDate != null) {
                list.add(
                        cb.lessThanOrEqualTo(
                                root.get("createTime").as(Date.class), endDate
                        )
                );
            }
            return cb.and(list.toArray(new Predicate[list.size()]));
        };
        return repository.findAll(condition);
    }

}
