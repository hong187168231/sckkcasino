package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.LunboPic;
import com.qianyi.casinocore.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class PictureService {
    @Autowired
    PictureRepository pictureRepository;

    public List<LunboPic> findAll() {
        return pictureRepository.findAll();
    }
}
