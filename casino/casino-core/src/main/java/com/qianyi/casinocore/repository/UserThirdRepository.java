package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserThird;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserThirdRepository extends JpaRepository<UserThird,Long> {
    UserThird findByUserId(Long userId);

    UserThird findByAccount(String account);

    UserThird findByGoldenfAccount(String account);

    @Query(value = "select * from user_third u where u.account is not null ",nativeQuery = true)
    List<UserThird> findAllAcount();

    @Query(value = "select * from user_third u where u.goldenf_account is not null ",nativeQuery = true)
    List<UserThird> findAllGoldenfAccount();
}
