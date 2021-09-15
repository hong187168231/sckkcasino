package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.GameRecordRepository;
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

    @Autowired
    private UserThirdService userThirdService;

    @PersistenceContext
    private EntityManager entityManager;

    public void saveAll(List<GameRecord> list) {
        for (GameRecord gameRecord : list) {
            try {
                //有数据会重复注单id唯一约束会报错，所以一条一条保存，避免影响后面的
                gameRecordRepository.save(gameRecord);
                //游戏记录保存成功后扣减打码量
                UserThird account = userThirdService.findByAccount(gameRecord.getUser());
                if (account == null) {
                    continue;
                }
                Long userId = account.getUserId();
                User user = userService.findById(userId);
                if (user == null) {
                    continue;
                }
                BigDecimal codeNum = user.getCodeNum();
                if (codeNum.compareTo(BigDecimal.ZERO) < 1) {
                    continue;
                }
                BigDecimal validbet = new BigDecimal(gameRecord.getValidbet());
                if (validbet.compareTo(codeNum) > -1) {
                    userService.subCodeNum(userId, codeNum);
                } else {
                    userService.subCodeNum(userId, validbet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        BigDecimal codeNum = new BigDecimal("-1");
        if(codeNum.compareTo(BigDecimal.ZERO)> -1){
            System.out.println(1);
        }
    }

    public Page<GameRecord> findGameRecordPage(Specification<GameRecord> condition, Pageable pageable){
        return gameRecordRepository.findAll(condition,pageable);
    }

    public List findRecordRecordSum(String user,String betId,String gname,Integer gid ){
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

        if (!CommonUtil.checkNull(betId)) {
            predicates.add(
                    builder.equal(root.get("betId").as(String.class), betId)
            );
        }
        if (!CommonUtil.checkNull(user)) {
            predicates.add(
                    builder.equal(root.get("user").as(String.class), user)
            );
        }
        if (!CommonUtil.checkNull(gname)) {
            predicates.add(
                    builder.equal(root.get("gname").as(String.class), gname)
            );
        }
        if (gid != null) {
            predicates.add(
                    builder.equal(root.get("gid").as(Integer.class), gid)
            );
        }
        query
                .where(predicates.toArray(new Predicate[predicates.size()]));
//                .groupBy(root.get("conversionStepCode"))
//                .orderBy(builder.desc(root.get("contactUserNums")));
        List<GameRecord> list = entityManager.createQuery(query).getResultList();
        return list;
    }
}
