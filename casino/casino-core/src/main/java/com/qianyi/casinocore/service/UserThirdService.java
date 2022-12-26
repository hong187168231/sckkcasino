package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.UserThirdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@CacheConfig(cacheNames = {"userThird"})
public class UserThirdService {
    @Autowired
    UserThirdRepository userThirdRepository;

//    @Cacheable(key = "#p0")
    public UserThird findByUserId(Long userId) {
        return userThirdRepository.findByUserId(userId);
    }
    @Caching(
        put = {@CachePut(key = "#result.userId"), @CachePut(key = "'findByAccount::'+#result.account",condition = "#result.account != null"),
            @CachePut(key = "'findByGoldenfAccount::'+#result.goldenfAccount",condition = "#result.goldenfAccount != null"),
            @CachePut(key = "'findByObdjAccount::'+#result.obdjAccount",condition = "#result.obdjAccount != null"),
            @CachePut(key = "'findByObtyAccount::'+#result.obtyAccount",condition = "#result.obtyAccount != null"),
            @CachePut(key = "'findByAEAccount::'+#result.aeAccount",condition = "#result.aeAccount != null"),
                @CachePut(key = "'findByDmcAccount::'+#result.dmcAccount",condition = "#result.dmcAccount != null"),
                @CachePut(key = "'findByDmcAccount::'+#result.dgAccount",condition = "#result.dgAccount != null"),
                @CachePut(key = "'findByVncAccount::'+#result.vncAccount",condition = "#result.vncAccount != null")
        }
    )
    public UserThird save(UserThird third) {
        return userThirdRepository.save(third);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByAccount(String account) {
        return userThirdRepository.findByAccount(account);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByGoldenfAccount(String account) {
        return userThirdRepository.findByGoldenfAccount(account);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByObdjAccount(String account) {
        return userThirdRepository.findByObdjAccount(account);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByObtyAccount(String account) {
        return userThirdRepository.findByObtyAccount(account);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByObzrAccount(String account) {
        return userThirdRepository.findByObzrAccount(account);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByAEAccount(String account) {
        return userThirdRepository.findByAeAccount(account);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByVNCAccount(String account) {
        return userThirdRepository.findByVncAccount(account);
    }

    public List<UserThird> findAllAcount(){
        return userThirdRepository.findAllAcount();
    }

    public List<UserThird> findAllGoldenfAccount(){
        return userThirdRepository.findAllGoldenfAccount();
    }

    public List<UserThird> findAllDgAccount(){
        return userThirdRepository.findAllDgAccount();
    }

    public List<UserThird> findAllOBDJAccount() {
        return userThirdRepository.findAllOBDJAccount();
    }

    public List<UserThird> findAllOBTYAccount() {
        return userThirdRepository.findAllOBTYAccount();
    }

    public List<UserThird> findAllOBZRAccount() {
        return userThirdRepository.findAllOBZRAccount();
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByVncAccount(String account) {
        return userThirdRepository.findByVncAccount(account);
    }
    public UserThird findByDmcAccount(String account) {
        return userThirdRepository.findByDmcAccount(account);
    }
    public UserThird findByDgAccount(String account) {
        return userThirdRepository.findByDgAccount(account);
    }
}