package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserThird;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserThirdRepository extends JpaRepository<UserThird,Long> {
    UserThird findByUserId(Long userId);

    UserThird findByAccount(String account);
}
