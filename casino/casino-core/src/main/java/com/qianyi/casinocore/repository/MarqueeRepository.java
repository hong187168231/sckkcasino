package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Marquee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MarqueeRepository extends JpaRepository<Marquee,Long> {
    Marquee findAllById(Long id);

    @Query("from Marquee m order by m.updateTime desc")
    List<Marquee> findByMarqueeList();
}
