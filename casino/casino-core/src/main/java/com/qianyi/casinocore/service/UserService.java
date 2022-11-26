package com.qianyi.casinocore.service;

import com.mysql.cj.util.StringUtils;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.UserRepository;
import com.qianyi.casinocore.util.*;
import com.qianyi.casinocore.vo.PersonReportVo;
import com.qianyi.casinocore.vo.RebateReportVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

@Service
@Transactional
@CacheConfig(cacheNames = {"user"})
@Slf4j
public class UserService {

    @Autowired
    UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public User findByAccount(String account) {
        return userRepository.findByAccount(account);
    }

    public User findOne(User user) {
        Specification<User> condition = this.getCondition(user, null, null);
        Optional<User> optional = userRepository.findOne(condition);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @CachePut(key = "#result.id", condition = "#result != null")
    public User save(User user) {
        return userRepository.save(user);
    }

    @CacheEvict(key = "#id")
    public void updatePassword(Long id, String password) {
        userRepository.updatePassword(id, password);
    }

    @CacheEvict(key = "#id")
    public void updateLevel(Long id, Integer level) {
        userRepository.updateLevel(id, level);
    }

    public List<User> saveAll(List<User> userList) {
        return userRepository.saveAll(userList);
    }

    @Cacheable(key = "#id")
    public User findById(Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @CacheEvict(key = "#id")
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public Integer countByIp(String ip) {
        return userRepository.countByRegisterIp(ip);
    }

    public User findUserByIdUseLock(Long userId) {
        return userRepository.findUserByUserIdUseLock(userId);
    }

    public User findUserByUserIdUse(Long userId) {
        return userRepository.findUserByUserIdUse(userId);
    }

    public void updateIsFirstBet(Long userId, Integer isFirstBet) {
        userRepository.updateIsFirstBet(userId, isFirstBet);
    }

    /**
     * 用户列表查询
     *
     * @param user
     * @return
     */
    public Page<User> findUserPage(Pageable pageable, User user, Date startDate, Date endDate) {
        Specification<User> condition = this.getCondition(user, startDate, endDate);
        return userRepository.findAll(condition, pageable);
    }

    public List<User> findUserList(User user, Date startDate, Date endDate) {
        Specification<User> condition = this.getCondition(user, startDate, endDate);
        return userRepository.findAll(condition);
    }

    public Long findUserCount(User user, Date startDate, Date endDate) {
        Specification<User> condition = this.getCondition(user, startDate, endDate);
        return userRepository.count(condition);
    }

    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param user
     * @return
     */
    private Specification<User> getCondition(User user, Date startDate, Date endDate) {
        Specification<User> specification = new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(user.getAccount())) {
                    list.add(cb.equal(root.get("account").as(String.class), user.getAccount()));
                }
                if (!CommonUtil.checkNull(user.getRegisterIp())) {
                    list.add(cb.equal(root.get("registerIp").as(String.class), user.getRegisterIp()));
                }
                if (user.getState() != null) {
                    list.add(cb.equal(root.get("state").as(String.class), user.getState()));
                }
                if (user.getRegisterDomainName() != null) {
                    list.add(cb.equal(root.get("registerDomainName").as(String.class), user.getRegisterDomainName()));
                }
                if (user.getId() != null) {
                    list.add(cb.equal(root.get("id").as(Long.class), user.getId()));
                }
                if (user.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), user.getFirstProxy()));
                }
                if (user.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), user.getSecondProxy()));
                }
                if (user.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), user.getThirdProxy()));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), endDate));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public List<User> findAll(List<Long> userIds) {
        Specification<User> condition = getCondition(userIds);
        List<User> userList = userRepository.findAll(condition);
        return userList;
    }

    private Specification<User> getCondition(List<Long> userIds) {
        Specification<User> specification = new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : userIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public List<User> findByRegisterIp(String ip) {
        return userRepository.findByRegisterIp(ip);
    }

    public User findByInviteCode(String inviteCode) {
        return userRepository.findByInviteCode(inviteCode);
    }

    public List<User> findByStateAndFirstPid(Integer state, Long firstPid) {
        return userRepository.findByStateAndFirstPid(state, firstPid);
    }

    public List<User> findByThirdPid(Long id) {
        return userRepository.findByThirdPid(id);
    }

    public List<User> findBySecondPid(Long id) {
        return userRepository.findBySecondPid(id);
    }

    public List<User> findFirstUser(Long id) {
        return userRepository.findByFirstPid(id);
    }

    public List<User> findFirstUserList(List<Long> userIds) {
        Specification<User> condition = getConditionFirstPid(userIds);
        List<User> userList = userRepository.findAll(condition);
        return userList;
    }

    private Specification<User> getConditionFirstPid(List<Long> userIds) {
        Specification<User> specification = new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("firstPid");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : userIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public Integer countByFirstPid(Long userId) {
        return userRepository.countByFirstPid(userId);
    }

    /**
     * 正常情况一个手机号只能注册一个号，这里是为了兼容历史数据，防止查询报错
     *
     * @param phone
     * @return
     */
    public List<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public User findByFirstPidAndAccount(Long userId, String account) {
        return userRepository.findByFirstPidAndAccount(userId, account);
    }

    @SuppressWarnings("unchecked")
    public List<PersonReportVo> findMap(String platform, String startTime, String endTime, Integer page,
        Integer pageSize, String sort, String orderTimeStart, String orderTimeEnd, String proxy) throws Exception {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        // if(StringUtils.isNullOrEmpty(platform)){
        // sql = MessageFormat.format(SqlConst.totalSql, startTime, endTime, sort, page.toString(),
        // pageSize.toString(),orderTimeStart,orderTimeEnd,proxy);
        // }else if (platform.equals("WM")){
        // sql = MessageFormat.format(SqlConst.wmSql,startTime, endTime, sort, page.toString(), pageSize.toString(),
        // "'wm'",proxy);
        // }else{
        // sql = MessageFormat.format(SqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(),
        // pageSize.toString(),"'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        // }
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(SqlConst.totalSqlReport, startTime, endTime, sort, page.toString(),
                pageSize.toString(), orderTimeStart, orderTimeEnd, proxy);// 走报表
            // sql = MessageFormat.format(SqlConst.totalSql, startTime, endTime, sort, page.toString(),
            // pageSize.toString(),proxy);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlConst.wmSql, startTime, endTime, sort, page.toString(), pageSize.toString(),
                "'wm'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlConst.obdjSql, startTime, endTime, sort, page.toString(), pageSize.toString(),
                "'OBDJ'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlConst.obtySql, startTime, endTime, sort, page.toString(), pageSize.toString(),
                "'OBTY'", proxy);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlConst.pgOrCq9Sql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'PG'", proxy);
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlConst.sabasportSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'SABASPORT'", proxy,"'Payoff'","'Stake'");
        } else {
            sql = MessageFormat.format(SqlConst.pgOrCq9Sql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'CQ9'", proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parsePersonReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, PersonReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public List<PersonReportVo> findMapBet(String platform, String startTime, String endTime, Integer page,
        Integer pageSize, String sort, String orderTimeStart, String orderTimeEnd, String proxy){
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlReport,  orderTimeStart, orderTimeEnd, sort, page.toString(),
                pageSize.toString(),proxy);// 走报表
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlNewConst.wmSql, startTime, endTime, sort, page.toString(),pageSize.toString(),proxy);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlNewConst.obdjSql, startTime, endTime, sort, page.toString(),pageSize.toString(),proxy);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlNewConst.obtySql, startTime, endTime, sort, page.toString(), pageSize.toString(),proxy);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlNewConst.pgOrCq9Sql, startTime, endTime, sort, page.toString(),
                pageSize.toString(),"'PG'",proxy);
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(SqlNewConst.aeMergeSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(),"'AE'",proxy);
        }  else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(SqlNewConst.aeSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(),"'HORSEBOOK'",proxy);
        }  else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(SqlNewConst.aeSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(),"'SV388'",proxy);
        }  else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(SqlNewConst.aeSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(),"'E1SPORT'",proxy);
        }  else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlNewConst.sabasportSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'SABASPORT'", proxy,"'Payoff'","'Stake'","'cancelPayoff'");
        }  else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(SqlNewConst.vncSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(),"'VNC'",proxy);
        }  else {
            sql = MessageFormat.format(SqlNewConst.pgOrCq9Sql, startTime, endTime, sort, page.toString(),
                pageSize.toString(),"'CQ9'", proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parsePersonBetMapList(resultList);
        return DTOUtil.map2DTO(mapList, PersonReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public List<PersonReportVo> findMapWash(String platform, String startTime, String endTime, Integer page,
        Integer pageSize, String sort,String proxy){
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash, startTime, endTime, sort, page.toString(),pageSize.toString(),proxy,"");
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash, startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'wm\' And");
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash, startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'OBDJ\' And");
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash, startTime, endTime, sort, page.toString(), pageSize.toString(),proxy," platform = \'OBTY\' And");
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash,startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'PG\' And");
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash,startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'AE\' And");
        } else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash,startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'HORSEBOOK\' And");
        } else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash,startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'SV388\' And");
        } else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash,startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'E1SPORT\' And");
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash, startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'SABASPORT\' And");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash, startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'VNC\' And");
        } else {
            sql = MessageFormat.format(SqlNewConst.totalSqlWash, startTime, endTime, sort, page.toString(),pageSize.toString(),proxy," platform = \'CQ9\' And");
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parsePersonWashMapList(resultList);
        return DTOUtil.map2DTO(mapList, PersonReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public List<PersonReportVo> findShareProfit(String startTime, String endTime, Integer page,
        Integer pageSize, String sort,String proxy){
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = MessageFormat.format(SqlShareConst.sqlShareProfit, startTime, endTime, sort, page.toString(),pageSize.toString(),proxy);
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parseShareProfitMapList(resultList);
        return DTOUtil.map2DTO(mapList, PersonReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public PersonReportVo findMapBet(String platform, String startTime, String endTime,String userId){
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId,"");
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'wm\'");
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'OBDJ\'");
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'OBTY\'");
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'PG\'");
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'AE\'");
        } else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'HORSEBOOK\'");
        } else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'SV388\'");
        } else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'E1SPORT\'");
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'SABASPORT\'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'VNC\'");
        } else {
            sql = MessageFormat.format(SqlNewConst.reportSql,  startTime, endTime, userId," And platform = \'CQ9\'");
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = parsePersonNotBetMapList(result);
        return DTOUtil.toDTO(map, PersonReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public PersonReportVo findMapWash(String platform, String startTime, String endTime,String userId, String orderTimeStart, String orderTimeEnd){
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(SqlNewConst.reportAllSql,  startTime, endTime, userId,"",orderTimeStart,orderTimeEnd);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlNewConst.reportWmSql,  startTime, endTime, userId," And platform = \'wm\'");
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlNewConst.reportObdjSql,  startTime, endTime, userId," And platform = \'OBDJ\'");
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlNewConst.reportObtySql,  startTime, endTime, userId," And platform = \'OBTY\'");
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlNewConst.reportPgOrCq9Sql,  startTime, endTime, userId," And platform = \'PG\'","'PG'");
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlNewConst.reportSabasportSql,  startTime, endTime, userId," And platform = \'SABASPORT\'","'SABASPORT'","'Payoff'","'Stake'","'cancelPayoff'");
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(SqlNewConst.reportAeMergeSql,  startTime, endTime, userId," And platform = \'AE\'","'AE'");
        } else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(SqlNewConst.reportAeSql,  startTime, endTime, userId," And platform = \'HORSEBOOK\'","'HORSEBOOK'");
        } else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(SqlNewConst.reportAeSql,  startTime, endTime, userId," And platform = \'SV388\'","'SV388'");
        } else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(SqlNewConst.reportAeSql,  startTime, endTime, userId," And platform = \'E1SPORT\'","'E1SPORT'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(SqlNewConst.reportVncSql,  startTime, endTime, userId," And platform = \'VNC\'","'VNC'");
        } else {
            sql = MessageFormat.format(SqlNewConst.reportPgOrCq9Sql,  startTime, endTime, userId," And platform = \'CQ9\'","'CQ9'");
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = parsePersonNotWashMapList(result);
        return DTOUtil.toDTO(map, PersonReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public PersonReportVo findShareProfit(String platform, String startTime, String endTime,String userId, String orderTimeStart, String orderTimeEnd){
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(SqlShareConst.reportAllSql,  startTime, endTime, userId,"",orderTimeStart,orderTimeEnd);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlShareConst.reportWmSql,  startTime, endTime, userId," And platform = \'wm\'");
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlNewConst.reportObdjSql,  startTime, endTime, userId," And platform = \'OBDJ\'");
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlNewConst.reportObtySql,  startTime, endTime, userId," And platform = \'OBTY\'");
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlNewConst.reportPgOrCq9Sql,  startTime, endTime, userId," And platform = \'PG\'","'PG'");
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlNewConst.reportSabasportSql,  startTime, endTime, userId," And platform = \'SABASPORT\'","'SABASPORT'","'Payoff'","'Stake'","'cancelPayoff'");
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(SqlNewConst.reportAeMergeSql,  startTime, endTime, userId," And platform = \'AE\'","'AE'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(SqlShareConst.reportVncSql,  startTime, endTime, userId," And platform = \'VNC\'","'VNC'");
        } else {
            sql = MessageFormat.format(SqlNewConst.reportPgOrCq9Sql,  startTime, endTime, userId," And platform = \'CQ9\'","'CQ9'");
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = parsePersonNotShareMapList(result);
        return DTOUtil.toDTO(map, PersonReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public List<PersonReportVo> findMapExport(String platform, String startTime, String endTime, String sort,
        String orderTimeStart, String orderTimeEnd, String proxy) throws Exception {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        // if(StringUtils.isNullOrEmpty(platform)){
        // sql = MessageFormat.format(SqlConst.totalSql, startTime, endTime, sort, page.toString(),
        // pageSize.toString(),orderTimeStart,orderTimeEnd,proxy);
        // }else if (platform.equals("WM")){
        // sql = MessageFormat.format(SqlConst.wmSql,startTime, endTime, sort, page.toString(), pageSize.toString(),
        // "'wm'",proxy);
        // }else{
        // sql = MessageFormat.format(SqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(),
        // pageSize.toString(),"'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        // }
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(SqlConst.exportTotalSqlReport, startTime, endTime, sort, orderTimeStart,
                orderTimeEnd, proxy);// 走报表
            // sql = MessageFormat.format(SqlConst.exportTotalSql, startTime, endTime, sort,proxy);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlConst.exportWmSql, startTime, endTime, sort, "'wm'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlConst.exportObdjSql, startTime, endTime, sort, "'OBDJ'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlConst.exportObtySql, startTime, endTime, sort, "'OBTY'", proxy);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlConst.exportPgOrCq9Sql, startTime, endTime, sort, "'PG'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(SqlConst.exportAeMergeSql, startTime, endTime, sort, "'AE'", proxy);
        }  else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(SqlConst.exportAeSql, startTime, endTime, sort, "'HORSEBOOK'", proxy);
        }  else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(SqlConst.exportAeSql, startTime, endTime, sort, "'SV388'", proxy);
        }  else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(SqlConst.exportAeSql, startTime, endTime, sort, "'E1SPORT'", proxy);
        }  else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlConst.exportSabasportSql, startTime, endTime, sort, "'SABASPORT'", proxy,"'Payoff'","'Stake'","'cancelPayoff'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(SqlConst.exportVncSql, startTime, endTime, sort, "'VNC'", proxy);
        }  else {
            sql = MessageFormat.format(SqlConst.exportPgOrCq9Sql, startTime, endTime, sort, "'CQ9'", proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parsePersonReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, PersonReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public List<PersonReportVo> findMap(String platform, String startTime, String endTime, Long userId,
        String orderTimeStart, String orderTimeEnd, String proxy) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        // if(StringUtils.isNullOrEmpty(platform)){
        // sql =
        // MessageFormat.format(SqlConst.seleOneTotal,startTime,endTime,userId.toString(),orderTimeStart,orderTimeEnd,proxy);
        // }else if (platform.equals("WM")){
        // sql = MessageFormat.format(SqlConst.seleOneWm, startTime, endTime, userId.toString(), "'wm'",proxy);
        // }else{
        // sql = MessageFormat.format(SqlConst.seleOnePgOrCq9Sql,startTime, endTime,
        // userId.toString(),"'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        // }
        if (StringUtils.isNullOrEmpty(platform)) {
            // 走报表
            sql = MessageFormat.format(SqlConst.seleOneTotalReport, startTime, endTime, userId.toString(),
                orderTimeStart, orderTimeEnd, proxy);
            // sql = MessageFormat.format(SqlConst.seleOneTotal,startTime,endTime,userId.toString(),proxy);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlConst.seleOneWm, startTime, endTime, userId.toString(), "'wm'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlConst.seleOneObdj, startTime, endTime, userId.toString(), "'OBDJ'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlConst.seleOneObty, startTime, endTime, userId.toString(), "'OBTY'", proxy);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql =
                MessageFormat.format(SqlConst.seleOnePgOrCq9Sql, startTime, endTime, userId.toString(), "'PG'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql =
                MessageFormat.format(SqlConst.seleOneAeMergeSql, startTime, endTime, userId.toString(), "'AE'", proxy);
        }  else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql =
                MessageFormat.format(SqlConst.seleOneAeSql, startTime, endTime, userId.toString(), "'HORSEBOOK'", proxy);
        }  else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql =
                MessageFormat.format(SqlConst.seleOneAeSql, startTime, endTime, userId.toString(), "'SV388'", proxy);
        }  else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql =
                MessageFormat.format(SqlConst.seleOneAeSql, startTime, endTime, userId.toString(), "'E1SPORT'", proxy);
        }  else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlConst.seleOneSabasportSql, startTime, endTime, userId.toString(),
                "'SABASPORT'", proxy,"'Payoff'","'Stake'","'cancelPayoff'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql =
                MessageFormat.format(SqlConst.seleOneVncSql, startTime, endTime, userId.toString(), "'VNC'", proxy);
        } else {
            sql =
                MessageFormat.format(SqlConst.seleOnePgOrCq9Sql, startTime, endTime, userId.toString(), "'CQ9'", proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parsePersonReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, PersonReportVo.class);
    }

    private static final List<String> PERSON_REPORT_TOTAL_FIELD_LIST = Arrays.asList("num", "bet_amount", "validbet",
        "win_loss", "wash_amount", "service_charge", "all_profit_amount", "avg_benefit", "total_amount", "all_water"
            , "todayAward", "riseAward");

    @SuppressWarnings("unchecked")
    public Map<String, Object> findMap(String platform, String startTime, String endTime, String orderTimeStart,
        String orderTimeEnd, String proxy) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        // if(StringUtils.isNullOrEmpty(platform)){
        // sql = MessageFormat.format(SqlConst.sumSql,startTime,endTime,orderTimeStart,orderTimeEnd, proxy);
        // }else if (platform.equals("WM")){
        // sql = MessageFormat.format(SqlConst.WMSumSql,startTime, endTime, "'wm'", proxy);
        // }else{
        // sql = MessageFormat.format(SqlConst.PGAndCQ9SumSql,startTime,
        // endTime,"'"+platform+"'",orderTimeStart,orderTimeEnd, proxy);
        // }
        if (StringUtils.isNullOrEmpty(platform)) {
            // 走报表
            sql = MessageFormat.format(SqlConst.sumSqlReport, startTime, endTime, orderTimeStart, orderTimeEnd, proxy);
            // sql = MessageFormat.format(SqlConst.sumSql,startTime,endTime, proxy);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlConst.WMSumSql, startTime, endTime, "'wm'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlConst.obdjSumSql, startTime, endTime, "'OBDJ'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlConst.obtySumSql, startTime, endTime, "'OBTY'", proxy);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlConst.PGAndCQ9SumSql, startTime, endTime, "'PG'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(SqlConst.aeSumMergeSql, startTime, endTime, "'AE'", proxy);
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(SqlConst.vncSumSql, startTime, endTime, "'VNC'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(SqlConst.aeSumSql, startTime, endTime, "'HORSEBOOK'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(SqlConst.aeSumSql, startTime, endTime, "'SV388'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(SqlConst.aeSumSql, startTime, endTime, "'E1SPORT'", proxy);
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlConst.sabasportSumSql, startTime, endTime, "'SABASPORT'", proxy,"'Payoff'","'Stake'","'cancelPayoff'");
        } else {
            sql = MessageFormat.format(SqlConst.PGAndCQ9SumSql, startTime, endTime, "'CQ9'", proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = new HashMap<>();
        Object[] obj = (Object[])result;
        for (int i = 0; i < PERSON_REPORT_TOTAL_FIELD_LIST.size(); i++) {
            String field = PERSON_REPORT_TOTAL_FIELD_LIST.get(i);
            Object value = obj[i];
            map.put(field, value);
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findMapSum(String platform, String startTime, String endTime, String orderTimeStart,
        String orderTimeEnd) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        if (StringUtils.isNullOrEmpty(platform)) {
            // 走报表
            sql = MessageFormat.format(SqlNewConst.sumSqlReport, startTime, endTime, orderTimeStart, orderTimeEnd);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(SqlNewConst.WMSumSql, startTime, endTime, "'wm'");
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(SqlNewConst.obdjSumSql, startTime, endTime, "'OBDJ'");
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(SqlNewConst.obtySumSql, startTime, endTime, "'OBTY'");
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(SqlNewConst.PGAndCQ9SumSql, startTime, endTime, "'PG'");
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(SqlNewConst.aeSumMergeSql, startTime, endTime, "'AE'");
        } else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(SqlNewConst.aeSumSql, startTime, endTime, "'HORSEBOOK'");
        } else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(SqlNewConst.aeSumSql, startTime, endTime, "'SV388'");
        } else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(SqlNewConst.aeSumSql, startTime, endTime, "'E1SPORT'");
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(SqlNewConst.sabasportSumSql, startTime, endTime, "'SABASPORT'","'Payoff'","'Stake'","'cancelPayoff'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(SqlNewConst.vncSumSql, startTime, endTime, "'VNC'");
        } else {
            sql = MessageFormat.format(SqlNewConst.PGAndCQ9SumSql, startTime, endTime, "'CQ9'");
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = new HashMap<>();
        Object[] obj = (Object[])result;
        for (int i = 0; i < PERSON_REPORT_TOTAL_FIELD_LIST.size(); i++) {
            String field = PERSON_REPORT_TOTAL_FIELD_LIST.get(i);
            Object value = obj[i];
            map.put(field, value);
        }
        return map;
    }


    private static final List<String> PERSON_REPORT_VO_FIELD_LIST =
        Arrays.asList("account", "third_proxy", "id", "num", "bet_amount", "validbet", "win_loss", "wash_amount",
            "service_charge", "all_profit_amount", "avg_benefit", "total_amount", "all_water", "todayAward", "riseAward");

    private List<Map<String, Object>> parsePersonReportMapList(List<Object> resultList) {
        List<Map<String, Object>> list = null;
        if (resultList != null && resultList.size() > CommonConst.NUMBER_0) {
            list = new LinkedList<>();

            for (Object result : resultList) {
                Map<String, Object> map = new HashMap<>();
                Object[] obj = (Object[])result;
                for (int i = 0; i < PERSON_REPORT_VO_FIELD_LIST.size(); i++) {
                    String field = PERSON_REPORT_VO_FIELD_LIST.get(i);
                    Object value = obj[i];
                    map.put(field, value);
                }
                list.add(map);
            }
        }
        return list;
    }

    private static final List<String> PERSON_REPORT_VO_FIELD_LIST_BET =
        Arrays.asList("account", "third_proxy", "id", "num", "bet_amount", "validbet", "win_loss");

    private List<Map<String, Object>> parsePersonBetMapList(List<Object> resultList) {
        List<Map<String, Object>> list = null;
        if (resultList != null && resultList.size() > CommonConst.NUMBER_0) {
            list = new LinkedList<>();
            Integer sort = 0;
            for (Object result : resultList) {
                Map<String, Object> map = new HashMap<>();
                Object[] obj = (Object[])result;
                for (int i = 0; i < PERSON_REPORT_VO_FIELD_LIST_BET.size(); i++) {
                    String field = PERSON_REPORT_VO_FIELD_LIST_BET.get(i);
                    Object value = obj[i];
                    map.put(field, value);
                }
                map.put("sort",sort++);
                list.add(map);
            }
        }
        return list;
    }

    private static final List<String> PERSON_REPORT_VO_FIELD_LIST_WASH =
        Arrays.asList("account", "third_proxy", "id", "wash_amount");

    private List<Map<String, Object>> parsePersonWashMapList(List<Object> resultList) {
        List<Map<String, Object>> list = null;
        if (resultList != null && resultList.size() > CommonConst.NUMBER_0) {
            list = new LinkedList<>();
            Integer sort = 0;
            for (Object result : resultList) {
                Map<String, Object> map = new HashMap<>();
                Object[] obj = (Object[])result;
                for (int i = 0; i < PERSON_REPORT_VO_FIELD_LIST_WASH.size(); i++) {
                    String field = PERSON_REPORT_VO_FIELD_LIST_WASH.get(i);
                    Object value = obj[i];
                    map.put(field, value);
                }
                map.put("sort",sort++);
                list.add(map);
            }
        }
        return list;
    }

    private static final List<String> PERSON_REPORT_VO_FIELD_LIST_SHARE_PROFIT =
        Arrays.asList("account", "third_proxy", "id", "all_profit_amount");

    private List<Map<String, Object>> parseShareProfitMapList(List<Object> resultList) {
        List<Map<String, Object>> list = null;
        if (resultList != null && resultList.size() > CommonConst.NUMBER_0) {
            list = new LinkedList<>();
            Integer sort = 0;
            for (Object result : resultList) {
                Map<String, Object> map = new HashMap<>();
                Object[] obj = (Object[])result;
                for (int i = 0; i < PERSON_REPORT_VO_FIELD_LIST_SHARE_PROFIT.size(); i++) {
                    String field = PERSON_REPORT_VO_FIELD_LIST_SHARE_PROFIT.get(i);
                    Object value = obj[i];
                    map.put(field, value);
                }
                map.put("sort",sort++);
                list.add(map);
            }
        }
        return list;
    }

    private static final List<String> PERSON_REPORT_VO_FIELD_LIST_NOTBET =
        Arrays.asList("wash_amount", "service_charge", "all_profit_amount", "all_water");

    private Map<String, Object> parsePersonNotBetMapList(Object result) {
        Map<String, Object> map = null;
        if (Objects.nonNull(result)) {
            map = new HashMap<>();
            Object[] obj = (Object[])result;
            for (int i = 0; i < PERSON_REPORT_VO_FIELD_LIST_NOTBET.size(); i++) {
                String field = PERSON_REPORT_VO_FIELD_LIST_NOTBET.get(i);
                Object value = obj[i];
                map.put(field, value);
            }
        }
        return map;
    }

    private static final List<String> PERSON_REPORT_VO_FIELD_LIST_NOTWASH =
        Arrays.asList("num", "bet_amount","validbet","win_loss","service_charge", "all_profit_amount", "all_water");

    private Map<String, Object> parsePersonNotWashMapList(Object result) {
        Map<String, Object> map = null;
        if (Objects.nonNull(result)) {
            map = new HashMap<>();
            Object[] obj = (Object[])result;
            for (int i = 0; i < PERSON_REPORT_VO_FIELD_LIST_NOTWASH.size(); i++) {
                String field = PERSON_REPORT_VO_FIELD_LIST_NOTWASH.get(i);
                Object value = obj[i];
                map.put(field, value);
            }
        }
        return map;
    }

    private static final List<String> PERSON_REPORT_VO_FIELD_LIST_NOTSHARE =
        Arrays.asList("num", "bet_amount","validbet","win_loss","service_charge", "wash_amount", "all_water");

    private Map<String, Object> parsePersonNotShareMapList(Object result) {
        Map<String, Object> map = null;
        if (Objects.nonNull(result)) {
            map = new HashMap<>();
            Object[] obj = (Object[])result;
            for (int i = 0; i < PERSON_REPORT_VO_FIELD_LIST_NOTSHARE.size(); i++) {
                String field = PERSON_REPORT_VO_FIELD_LIST_NOTSHARE.get(i);
                Object value = obj[i];
                map.put(field, value);
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public List<RebateReportVo> findRebateMap(String platform, String startTime, String endTime, Integer page,
        Integer pageSize, String sort, String orderTimeStart, String orderTimeEnd, String proxy) throws Exception {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        // if(StringUtils.isNullOrEmpty(platform)){
        // sql = MessageFormat.format(RebateSqlConst.totalSql, startTime, endTime, sort, page.toString(),
        // pageSize.toString(),orderTimeStart,orderTimeEnd,proxy);
        // }else if (platform.equals("WM")){
        // sql = MessageFormat.format(RebateSqlConst.wmSql,startTime, endTime, sort, page.toString(),
        // pageSize.toString(), "'wm'",proxy);
        // }else{
        // sql = MessageFormat.format(RebateSqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(),
        // pageSize.toString(), "'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        // }
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(RebateSqlConst.totalSqlReport, startTime, endTime, sort, page.toString(),
                pageSize.toString(), orderTimeStart, orderTimeEnd, proxy);
            // sql = MessageFormat.format(RebateSqlConst.totalSql, startTime, endTime, sort, page.toString(),
            // pageSize.toString(),proxy);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(RebateSqlConst.wmSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'wm'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(RebateSqlConst.obdjSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'OBDJ'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(RebateSqlConst.obtySql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'OBTY'", proxy);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(RebateSqlConst.pgOrCq9Sql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'PG'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(RebateSqlConst.aeMergeSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'AE'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(RebateSqlConst.aeSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'HORSEBOOK'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(RebateSqlConst.aeSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'SV388'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(RebateSqlConst.aeSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'E1SPORT'", proxy);
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(RebateSqlConst.sabasportSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'SABASPORT'", proxy,"'Payoff'","'Stake'","'cancelPayoff'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(RebateSqlConst.vncSql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'VNC'", proxy);
        } else {
            sql = MessageFormat.format(RebateSqlConst.pgOrCq9Sql, startTime, endTime, sort, page.toString(),
                pageSize.toString(), "'CQ9'", proxy);
        }
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parseRebateReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, RebateReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public List<RebateReportVo> findRebateMapExport(String platform, String startTime, String endTime, String sort,
        String orderTimeStart, String orderTimeEnd, String proxy) throws Exception {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(RebateSqlConst.exportTotalSqlReport, startTime, endTime, sort, orderTimeStart,
                orderTimeEnd, proxy);
            // sql = MessageFormat.format(RebateSqlConst.exportTotalSql, startTime, endTime, sort,proxy);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(RebateSqlConst.exportWmSql, startTime, endTime, sort, "'wm'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(RebateSqlConst.exportObdjSql, startTime, endTime, sort, "'OBDJ'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(RebateSqlConst.exportObtySql, startTime, endTime, sort, "'OBTY'", proxy);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(RebateSqlConst.exportPgOrCq9Sql, startTime, endTime, sort, "'PG'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(RebateSqlConst.exportAeMergeSql, startTime, endTime, sort, "'AE'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(RebateSqlConst.exportAeSql, startTime, endTime, sort, "'HORSEBOOK'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(RebateSqlConst.exportAeSql, startTime, endTime, sort, "'SV388'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(RebateSqlConst.exportAeSql, startTime, endTime, sort, "'E1SPORT'", proxy);
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql =
                MessageFormat.format(RebateSqlConst.exportSabasportSql, startTime, endTime, sort, "'SABASPORT'", proxy,"'Payoff'","'Stake'","'cancelPayoff'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(RebateSqlConst.exportVncSql, startTime, endTime, sort, "'VNC'", proxy);
        }else {
            sql = MessageFormat.format(RebateSqlConst.exportPgOrCq9Sql, startTime, endTime, sort, "'CQ9'", proxy);
        }
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parseRebateReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, RebateReportVo.class);
    }

    @SuppressWarnings("unchecked")
    public List<RebateReportVo> findRebateMap(String platform, String startTime, String endTime, Long userId,
        String orderTimeStart, String orderTimeEnd, String proxy) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        // if(StringUtils.isNullOrEmpty(platform)){
        // sql =
        // MessageFormat.format(RebateSqlConst.seleOneTotal,startTime,endTime,userId.toString(),orderTimeStart,orderTimeEnd,proxy);
        // }else if (platform.equals("WM")){
        // sql = MessageFormat.format(RebateSqlConst.seleOneWm, startTime, endTime, userId.toString(), "'wm'",proxy);
        // }else{
        // sql = MessageFormat.format(RebateSqlConst.seleOnePgOrCq9Sql,startTime, endTime, userId.toString(),
        // "'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        // }
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneTotalReport, startTime, endTime, userId.toString(),
                orderTimeStart, orderTimeEnd, proxy);
            // sql = MessageFormat.format(RebateSqlConst.seleOneTotal,startTime,endTime,userId.toString(),proxy);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneWm, startTime, endTime, userId.toString(), "'wm'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneObdj, startTime, endTime, userId.toString(), "'OBDJ'",
                proxy);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneObty, startTime, endTime, userId.toString(), "'OBTY'",
                proxy);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(RebateSqlConst.seleOnePgOrCq9Sql, startTime, endTime, userId.toString(), "'PG'",
                proxy);
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneAeMergeSql, startTime, endTime, userId.toString(), "'AE'",
                proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneAeSql, startTime, endTime, userId.toString(), "'HORSEBOOK'",
                proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneAeSql, startTime, endTime, userId.toString(), "'SV388'",
                proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneAeSql, startTime, endTime, userId.toString(), "'E1SPORT'",
                proxy);
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneSabasportSql, startTime, endTime, userId.toString(),
                "'SABASPORT'", proxy,"'Payoff'","'Stake'","'cancelPayoff'");
        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(RebateSqlConst.seleOneVncSql, startTime, endTime, userId.toString(), "'VNC'",
                proxy);
        } else {
            sql = MessageFormat.format(RebateSqlConst.seleOnePgOrCq9Sql, startTime, endTime, userId.toString(), "'CQ9'",
                proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parseRebateReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, RebateReportVo.class);
    }

    private static final List<String> REBATE_REPORT_TOTAL_FIELD_LIST = Arrays.asList("num", "bet_amount", "validbet",
        "win_loss", "total_rebate", "user_amount", "surplus_amount", "service_charge", "avg_benefit", "total_amount");

    @SuppressWarnings("unchecked")
    public Map<String, Object> findRebateMap(String platform, String startTime, String endTime, String orderTimeStart,
        String orderTimeEnd, String proxy) {
        startTime = "'" + startTime + "'";
        endTime = "'" + endTime + "'";
        String sql = "";
        if (StringUtils.isNullOrEmpty(platform)) {
            sql = MessageFormat.format(RebateSqlConst.sumSqlReport, startTime, endTime, orderTimeStart, orderTimeEnd,
                proxy);
            // sql = MessageFormat.format(RebateSqlConst.sumSql,startTime,endTime,proxy);
        } else if (platform.equals(Constants.PLATFORM_WM_BIG)) {
            sql = MessageFormat.format(RebateSqlConst.WMSumSql, startTime, endTime, "'wm'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBDJ)) {
            sql = MessageFormat.format(RebateSqlConst.obdjSumSql, startTime, endTime, "'OBDJ'", proxy);
        } else if (platform.equals(Constants.PLATFORM_OBTY)) {
            sql = MessageFormat.format(RebateSqlConst.obtySumSql, startTime, endTime, "'OBTY'", proxy);
        } else if (platform.equals(Constants.PLATFORM_PG)) {
            sql = MessageFormat.format(RebateSqlConst.PGAndCQ9SumSql, startTime, endTime, "'PG'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE)) {
            sql = MessageFormat.format(RebateSqlConst.aeSumMergeSql, startTime, endTime, "'AE'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_HORSEBOOK)) {
            sql = MessageFormat.format(RebateSqlConst.aeSumSql, startTime, endTime, "'HORSEBOOK'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_SV388)) {
            sql = MessageFormat.format(RebateSqlConst.aeSumSql, startTime, endTime, "'SV388'", proxy);
        } else if (platform.equals(Constants.PLATFORM_AE_E1SPORT)) {
            sql = MessageFormat.format(RebateSqlConst.aeSumSql, startTime, endTime, "'E1SPORT'", proxy);
        } else if (platform.equals(Constants.PLATFORM_SABASPORT)) {
            if (StringUtils.isNullOrEmpty(proxy)){
                sql = MessageFormat.format(RebateSqlConst.sabasportAdminSumSql, startTime, endTime, "'SABASPORT'","'Payoff'","'Stake'","'cancelPayoff'");
            }else {
                sql = MessageFormat.format(RebateSqlConst.sabasportProxySumSql, startTime, endTime, "'SABASPORT'", proxy,"'Payoff'","'Stake'");
            }

        } else if (platform.equals(Constants.PLATFORM_VNC)) {
            sql = MessageFormat.format(RebateSqlConst.vncSumSql, startTime, endTime, "'VNC'", proxy);
        } else {
            sql = MessageFormat.format(RebateSqlConst.PGAndCQ9SumSql, startTime, endTime, "'CQ9'", proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String, Object> map = new HashMap<>();
        Object[] obj = (Object[])result;
        for (int i = 0; i < REBATE_REPORT_TOTAL_FIELD_LIST.size(); i++) {
            String field = REBATE_REPORT_TOTAL_FIELD_LIST.get(i);
            Object value = obj[i];
            map.put(field, value);
        }

        return map;
    }

    private static final List<String> REBATE_REPORT_VO_FIELD_LIST =
        Arrays.asList("account", "third_proxy", "id", "num", "bet_amount", "validbet", "win_loss", "total_rebate",
            "user_amount", "surplus_amount", "service_charge", "avg_benefit", "total_amount");

    private List<Map<String, Object>> parseRebateReportMapList(List<Object> resultList) {
        List<Map<String, Object>> list = null;
        if (resultList != null && resultList.size() > CommonConst.NUMBER_0) {
            list = new LinkedList<>();

            for (Object result : resultList) {
                Map<String, Object> map = new HashMap<>();
                Object[] obj = (Object[])result;
                for (int i = 0; i < REBATE_REPORT_VO_FIELD_LIST.size(); i++) {
                    String field = REBATE_REPORT_VO_FIELD_LIST.get(i);
                    Object value = obj[i];
                    map.put(field, value);
                }
                list.add(map);
            }
        }
        return list;
    }

    public Set<Long> findUserByRegisterDomainName(String registerDomainName, String startTime, String endTime) {
        return userRepository.findUserByRegisterDomainName(registerDomainName, startTime, endTime);
    }

    public List<User> findByAccountUpper(String account) {
        return userRepository.findByAccountUpper(account);
    }

    public long fingCount() {
        return userRepository.count();
    }

}
