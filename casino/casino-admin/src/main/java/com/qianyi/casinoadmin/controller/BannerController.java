//package com.qianyi.casinoadmin.controller;
//
//
//import com.qianyi.casinoadmin.util.LoginUtil;
//import com.qianyi.casinocore.model.Banner;
//import com.qianyi.casinocore.service.BannerService;
//import com.qianyi.casinoadmin.util.CommonConst;
//import com.qianyi.casinocore.service.SysUserService;
//import com.qianyi.modulecommon.reponse.ResponseEntity;
//import com.qianyi.modulecommon.reponse.ResponseUtil;
//import com.qianyi.modulecommon.util.CommonUtil;
//import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import java.util.*;
//
//@RestController
//@RequestMapping("/banner")
//@Api(tags = "Banner设置")
//public class BannerController {
//    private static final Logger logger = LoggerFactory.getLogger(BannerService.class);
//    @Autowired
//    private BannerService bannerService;
//    @Autowired
//    private SysUserService sysUserService;
//
//    @ApiOperation("修改Banner")
//    @PostMapping(value = "/updateBanner",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "修改Banner")
//    public ResponseEntity updateBanner(@RequestPart("files") MultipartFile[] files,@RequestParam(value = "id") Integer id,@RequestParam(value = "文章链接") String articleLink) {
//        if (id == null){
//            return ResponseUtil.custom(CommonConst.IDNOTNULL);
//        }
//        Banner banner = bannerService.findAllById(id);
//        banner.setArticleLink(articleLink);
//        return this.getfilePaths(files,banner);
//    }
//    @ApiOperation("新增Banner")
//    @PostMapping(value = "/saveBanner",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "新增Banner")
//    public ResponseEntity saveBanner(@RequestPart(value = "file") MultipartFile[] files,@RequestParam(value = "展示端1 web 2 app") Integer theShowEnd,
//                                     @RequestParam(value = "文章链接") String articleLink){
//        Banner banner = new Banner();
//        banner.setTheShowEnd(theShowEnd);
//        banner.setArticleLink(articleLink);
//        banner.setHits(CommonConst.NUMBER_0);
//        return this.getfilePaths(files,banner);
//    }
//
//    @ApiOperation("删除Banner")
//    @GetMapping("/deleteBanner")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "主键id", required = true),
//    })
//    public ResponseEntity deleteBanner(Integer id){
//        if (id == null){
//            return ResponseUtil.custom(CommonConst.IDNOTNULL);
//        }
//        bannerService.deleteById(id);
//        return ResponseUtil.success();
//    }
//
//    @ApiOperation("查找Banner")
//    @GetMapping("/findByBannerList")
//    public ResponseEntity findByBannerList(){
//        return ResponseUtil.success(bannerService.findByBannerList());
//    }
//
//    private ResponseEntity getfilePaths(MultipartFile[] files,Banner banner){
//        Map<Integer,String> map = new HashMap<>();
//        try {
//            if (files == null||files.length >= CommonConst.NUMBER_6){
//                return ResponseUtil.custom(CommonConst.PICTURENOTUP);
//            }
//            int limit = CommonConst.NUMBER_0;
//            for(MultipartFile file:files){
//                String fileUrl = UploadAndDownloadUtil.fileUpload(CommonUtil.getLocalPicPath(), file);
//                map.put(limit++,fileUrl);
//            }
//            if (map.size() == CommonConst.NUMBER_0){
//                return ResponseUtil.custom(CommonConst.PICTURENOTUP);
//            }
//            banner.setFirstMap(map.get(CommonConst.NUMBER_0));
//            banner.setSecondMap(map.get(CommonConst.NUMBER_1));
//            banner.setThirdlyMap(map.get(CommonConst.NUMBER_2));
//            banner.setFourthlyMap(map.get(CommonConst.NUMBER_3));
//            banner.setFifthMap(map.get(CommonConst.NUMBER_4));
//            banner.setLastUpdatedBy(sysUserService.findAllById(LoginUtil.getLoginUserId()).getUserName());
//            banner.setLastUpdatedTime(new Date());
//        } catch (Exception e) {
//            logger.error("处理图片出错", e);
//            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
//        }
//        bannerService.saveBanner(banner);
//        return ResponseUtil.success();
//    }
//}