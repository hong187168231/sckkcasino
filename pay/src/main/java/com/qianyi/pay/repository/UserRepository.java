package com.qianyi.pay.repository;

import com.qianyi.pay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
