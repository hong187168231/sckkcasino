package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.repository.ProxyUserRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"proxyUser"})
public class ProxyUserService {

    @Autowired
    private ProxyUserRepository proxyUserRepository;

    public List<ProxyUser> findAll() {
        return proxyUserRepository.findAll();
    }

    public ProxyUser findByUserName(String userName) {
        return proxyUserRepository.findByUserName(userName);
    }

    public void setSecretById(Long id, String gaKey) {
        proxyUserRepository.setSecretById(id, gaKey);
    }
    @CachePut(key="#result.id",condition = "#result != null")
    public void save(ProxyUser proxyUser) {
        proxyUserRepository.save(proxyUser);
    }
    @Cacheable(key = "#id")
    public ProxyUser findAllById(Long id){
        return proxyUserRepository.findAllById(id);
    }
    @Cacheable(key = "#id")
    public ProxyUser findById(Long id){
        Optional<ProxyUser> optional = proxyUserRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
    /**
     * 用户列表查询
     *
     * @param proxyUser
     * @return
     */
    public Page<ProxyUser> findUserPage(Pageable pageable, ProxyUser proxyUser, Date startDate, Date endDate) {
        Specification<ProxyUser> condition = this.getCondition(proxyUser,startDate,endDate);
        return proxyUserRepository.findAll(condition, pageable);
    }
    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param proxyUser
     * @return
     */
    private Specification<ProxyUser> getCondition(ProxyUser proxyUser, Date startDate, Date endDate) {
        Specification<ProxyUser> specification = new Specification<ProxyUser>() {
            @Override
            public Predicate toPredicate(Root<ProxyUser> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(proxyUser.getUserName())) {
                    list.add(cb.equal(root.get("userName").as(String.class), proxyUser.getUserName()));
                }
                if (proxyUser.getProxyRole() != null) {
                    list.add(cb.equal(root.get("proxyRole").as(Integer.class), proxyUser.getProxyRole()));
                }
                if (proxyUser.getUserFlag() != null) {
                    list.add(cb.equal(root.get("userFlag").as(Integer.class), proxyUser.getUserFlag()));
                }
                if (proxyUser.getId() != null) {
                    list.add(cb.equal(root.get("id").as(Long.class), proxyUser.getId()));
                }
                if (proxyUser.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), proxyUser.getFirstProxy()));
                }
                if (proxyUser.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), proxyUser.getSecondProxy()));
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
}
