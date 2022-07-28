package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.IpWhite;
import com.qianyi.casinocore.repository.IpWhiteRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
//@CacheConfig(cacheNames = {"ipWhite"})
@Transactional
public class IpWhiteService {

    @Autowired
    private IpWhiteRepository ipWhiteRepository;

//    @CachePut(key="#po.ip+'::'+#po.type")
    public IpWhite save(IpWhite po){
        return ipWhiteRepository.save(po);
    }

//    @Cacheable(key = "#ip+'::'+#type")
    public IpWhite findByIpAndType(String ip, Integer type) {
        return ipWhiteRepository.findByIpAndType(ip,type);
    }

//    @CacheEvict(key="#po.ip+'::'+#po.type")
    public void delete(IpWhite po){
        ipWhiteRepository.delete(po);
    }

    public IpWhite findById(Long id){
        Optional<IpWhite> byId = ipWhiteRepository.findById(id);
        if (byId.isPresent()){
            return byId.get();
        }
        return null;
    }

    public List<IpWhite> findAll(){
        return ipWhiteRepository.findAll();
    }

    public Page<IpWhite> findPage(IpWhite ipWhite , Pageable pageable){
        Specification<IpWhite> condition = this.getCondition(ipWhite);
        return ipWhiteRepository.findAll(condition,pageable);
    }

    private Specification<IpWhite> getCondition(IpWhite ipWhite) {
        Specification<IpWhite> specification = new Specification<IpWhite>() {
            @Override
            public Predicate toPredicate(Root<IpWhite> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(ipWhite.getIp())) {
                    list.add(cb.equal(root.get("ip").as(String.class), ipWhite.getIp()));
                }
                if (ipWhite.getType() !=null) {
                    list.add(cb.equal(root.get("type").as(Long.class), ipWhite.getType()));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
}
