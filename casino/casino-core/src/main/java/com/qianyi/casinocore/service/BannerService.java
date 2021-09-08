//package com.qianyi.casinocore.service;
//
//import com.qianyi.casinocore.model.Banner;
//import com.qianyi.casinocore.repository.BannerRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//public class BannerService {
//
//    @Autowired
//    private BannerRepository bannerRepository;
//
//    public Banner findAllById(Integer id){
//        return bannerRepository.findAllById(id);
//    }
//    public List<Banner> findByBannerList(){
//        return bannerRepository.findByBannerList();
//    }
//
//    public void deleteById(Integer id){
//        bannerRepository.deleteById(id);
//    }
//
//    public void saveBanner(Banner banner) {
//        bannerRepository.saveAndFlush(banner);
//    }
//
//
//
//}
