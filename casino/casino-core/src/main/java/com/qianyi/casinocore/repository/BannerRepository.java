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

    @Modifying
    @Transactional
    void deleteById(Integer id);

    @Query("update Banner b set b.firstMap=?2,b.secondMap=?3,b.thirdlyMap=?4,b.fourthlyMap=?5,b.fifthMap=?6,b.articleLink=?7,b.lastUpdatedBy=?8 where b.id=?1")
    @Modifying
    @Transactional
    void updateById(Integer id, String firstMap,String secondMap,String thirdlyMap,String fourthlyMap,String fifthMap,String articleLink,String account);

    @Modifying
    Banner saveAndFlush(Banner banner);
}
