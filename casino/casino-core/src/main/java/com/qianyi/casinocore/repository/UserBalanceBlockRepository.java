package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserBalanceBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.OrderBy;
import java.util.List;

public interface UserBalanceBlockRepository  extends JpaRepository<UserBalanceBlock,Long> {

    @OrderBy("id DESC ")
    List<UserBalanceBlock> findByUserName(String userName);

    @Query("update UserBalanceBlock u set u.status=u.status-?2 where u.id=?1")
    @Modifying
    void updateStatus(Integer status, Long id);
}
