package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
