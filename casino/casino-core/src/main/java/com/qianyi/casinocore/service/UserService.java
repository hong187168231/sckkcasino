package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.repository.UserRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@CacheConfig(cacheNames = {"user"})
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User findByAccount(String account) {
        return userRepository.findByAccount(account);
    }

    @CachePut(key="#result.id",condition = "#result != null")
    public User save(User user) {
        return userRepository.save(user);
    }

    @Cacheable(key = "#id")
    public User findById(Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @CacheEvict(key="#id")
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }


    public Integer countByIp(String ip) {
        return userRepository.countByRegisterIp(ip);
    }

    public User findUserByIdUseLock(Long userId) {
        return userRepository.findUserByUserIdUseLock(userId);
    }


    /**
     * 用户列表查询
     *
     * @param user
     * @return
     */
    public Page<User> findUserPage(Pageable pageable, User user, Date startDate,Date endDate) {
        Specification<User> condition = this.getCondition(user,startDate,endDate);
        return userRepository.findAll(condition, pageable);
    }

    /**
     * 根据注册ip用户列表查询
     *
     * @param user
     * @return
     */
    public List<User> findUserList(User user) {
        Specification<User> condition = this.getCondition(user,null,null);
        return userRepository.findAll(condition);
    }
    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param user
     * @return
     */
    private Specification<User> getCondition(User user,Date startDate,Date endDate) {
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
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
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

    public List<User> findByStateAndFirstPid(Integer state,Long firstPid){
        return userRepository.findByStateAndFirstPid(state,firstPid);
    }

    public List<User> findByStateAndSecondPid(Integer state,Long secondPid){
        return userRepository.findByStateAndSecondPid(state,secondPid);
    }

    public List<User> findByStateAndThirdPid(Integer state,Long thirdPid){
        return userRepository.findByStateAndThirdPid(state,thirdPid);
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
     * @param phone
     * @return
     */
    public List<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public List<User> findByFirstPidAndAccount(Long userId,String account) {
        return userRepository.findByFirstPidAndAccount(userId,account);
    }
}
