package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Marquee;
import com.qianyi.casinocore.repository.MarqueeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarqueeService {
    @Autowired
    private MarqueeRepository marqueeRepository;

    public void deleteById(Long id){
        marqueeRepository.deleteById(id);
    }

    public void saveMarquee(Marquee marquee){
        marqueeRepository.save(marquee);
    }

    public List<Marquee> findByMarqueeList(){
        return marqueeRepository.findByMarqueeList();
    }

    public Marquee findAllById(Long id){
        return marqueeRepository.findAllById(id);
    }
}
