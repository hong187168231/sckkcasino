package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Notice;
import com.qianyi.casinocore.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class NoticeService {

    @Autowired
    NoticeRepository noticeRepository;

    /**
     * 最新公告
     * @return
     */
    public List<Notice> newest() {
        return noticeRepository.newest();
    }

    public Notice saveNotice(Notice notice){
        return noticeRepository.save(notice);
    }

    public List<Notice> findByNoticeList(){
        return noticeRepository.findByNoticeList();
    }

    public void deleteById(Long id){
        noticeRepository.deleteById(id);
    }

    public Notice findNoticeById(Long id){
        return  noticeRepository.findAllById(id);
    }
}
