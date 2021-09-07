package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Banner;
import com.qianyi.casinocore.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    public List<Banner> findByBannerList(){
        return bannerRepository.findByBannerList();
    }

    public void deleteById(Integer id){
        bannerRepository.deleteById(id);
    }

    public void updateById(Integer id, String articleLink, Map<Integer,String> map) {
        bannerRepository.updateById(id,map.get(0), map.get(1),map.get(2),map.get(3), map.get(4),articleLink);
    }

    public void saveBanner(Banner banner) {
        bannerRepository.saveAndFlush(banner);
    }

}
