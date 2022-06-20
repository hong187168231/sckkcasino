package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyUser;
import org.springframework.data.jpa.repository.*;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

public interface ProxyUserRepository extends JpaRepository<ProxyUser,Long>, JpaSpecificationExecutor<ProxyUser> {

    ProxyUser findByUserName(String userName);

    @Transactional
    @Query("update ProxyUser u set u.gaKey=?2 where u.id=?1")
    @Modifying
    void setSecretById(Long id, String gaKey);

    @Modifying
    @Query("update ProxyUser p set p.proxyUsersNum=p.proxyUsersNum+1 where p.id=?1")
    void addProxyUsersNum(Long proxyUserId);

    @Modifying
    @Query("update ProxyUser p set p.proxyUsersNum=p.proxyUsersNum-1 where p.id=?1")
    void subProxyUsersNum(Long proxyUserId);

    @Modifying
    @Query("update ProxyUser p set p.proxyUsersNum=0 where p.id=?1")
    void makeZero(Long proxyUserId);

    @Modifying
    @Query("update ProxyUser p set p.proxyUsersNum=p.proxyUsersNum+?2 where p.id=?1")
    void addProxyUsersNum(Long proxyUserId,Integer num);

    @Modifying
    @Query("update ProxyUser p set p.proxyUsersNum=p.proxyUsersNum-?2 where p.id=?1")
    void subProxyUsersNum(Long proxyUserId,Integer num);

    ProxyUser findAllById(Long id);

    ProxyUser findByProxyCode(String inviteCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    ProxyUser  findProxyUserById(Long id);

    List<ProxyUser> findByCompanyId(Long id);

    @Query(value = "select pr.id from proxy_user pr where pr.company_id in (?1) and proxy_role = 1;",nativeQuery = true)
    List<Long> findByCompanyIdList(Set<Long> idList);

    @Query(value = "select pr.user_name as userName from proxy_user pr where pr.company_id in (?1) and proxy_role = 1;",nativeQuery = true)
    List<String> findByCompanyRole(Long id);

}
