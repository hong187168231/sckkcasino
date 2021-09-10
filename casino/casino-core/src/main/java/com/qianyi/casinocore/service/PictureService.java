package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.LunboPic;
import com.qianyi.casinocore.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PictureService {
    @Autowired
    private PictureRepository pictureRepository;

    public List<LunboPic> findAll() {
        return pictureRepository.findAll();
    }

    public List<LunboPic> findAll(Specification<LunboPic> condition) {
        return pictureRepository.findAll(condition);
    }
    public List<LunboPic> findByLunboPicList(){
        return pictureRepository.findByLunboPicList();
    }
    public LunboPic findLunboPicbyId(Long id){
        return pictureRepository.findAllById(id);
    }
    public void deleteById(Long id){
        pictureRepository.deleteById(id);
    }
    public void save(LunboPic lunboPic){
        pictureRepository.save(lunboPic);
    }

    public List<LunboPic> findByTheShowEnd(Integer theShowEnd) {
        return pictureRepository.findByTheShowEnd(theShowEnd);
    }
}
