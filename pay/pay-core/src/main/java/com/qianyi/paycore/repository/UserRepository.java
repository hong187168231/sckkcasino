package com.qianyi.paycore.repository;

import com.qianyi.paycore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
