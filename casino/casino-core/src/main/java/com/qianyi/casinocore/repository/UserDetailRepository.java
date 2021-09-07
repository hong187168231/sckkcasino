package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserDetailRepository extends JpaRepository<UserDetail,Long>, JpaSpecificationExecutor<UserDetail> {

    UserDetail findByUserName(String userName);

    UserDetail findByUserId(String userId);
}
