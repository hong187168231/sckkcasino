package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserThird;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserThirdRepository extends JpaRepository<UserThird,Long> {
    UserThird findByUserId(Long userId);

    UserThird findByAccount(String account);

    UserThird findByGoldenfAccount(String account);

    UserThird findByObdjAccount(String account);

    UserThird findByObtyAccount(String account);
    UserThird findByObzrAccount(String account);

    UserThird findByAeAccount(String account);

    @Query(value = "select * from user_third u where u.account is not null ",nativeQuery = true)
    List<UserThird> findAllAcount();

    @Query(value = "select * from user_third u where u.goldenf_account is not null ",nativeQuery = true)
    List<UserThird> findAllGoldenfAccount();

    @Query(value = "select * from user_third u where u.dg_account is not null ",nativeQuery = true)
    List<UserThird> findAllDgAccount();

    @Query(value = "select * from user_third u where u.obdj_account is not null ",nativeQuery = true)
    List<UserThird> findAllOBDJAccount();

    @Query(value = "select * from user_third u where u.obty_account is not null ",nativeQuery = true)
    List<UserThird> findAllOBTYAccount();

    @Query(value = "select * from user_third u where u.obzr_account is not null ",nativeQuery = true)
    List<UserThird> findAllOBZRAccount();

    UserThird findByVncAccount(String account);

    UserThird findByDmcAccount(String account);

    UserThird findByDgAccount(String account);
}