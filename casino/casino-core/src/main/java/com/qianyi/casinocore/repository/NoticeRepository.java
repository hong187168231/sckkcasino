package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("from Notice n where n.isShelves=true order by n.createTime desc")
    List<Notice> newest();
}
