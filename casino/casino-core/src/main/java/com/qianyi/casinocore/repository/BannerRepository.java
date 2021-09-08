package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner,Long> {
    @Query("from Banner b order by b.lastUpdatedTime desc")
    List<Banner> findByBannerList();

    Banner findAllById(Integer id);

    @Modifying
    @Transactional
    void deleteById(Integer id);

    @Modifying
    @Transactional
    Banner saveAndFlush(Banner banner);
}
