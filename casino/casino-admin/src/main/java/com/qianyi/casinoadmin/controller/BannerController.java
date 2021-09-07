package com.qianyi.casinoadmin.controller;


import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.Banner;
import com.qianyi.casinocore.service.BannerService;
import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/banner")
@Api(tags = "Banner设置")
public class BannerController {
    private static final Logger logger = LoggerFactory.getLogger(BannerService.class);
    @Autowired
    private BannerService bannerService;
    @Autowired
    private UserService userService;

    @ApiOperation("修改Banner")
    @PostMapping("/updateBanner")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "banner主键", required = true),
            @ApiImplicitParam(name = "articleLink", value = "文章链接", required = true),
    })
    public ResponseEntity uploadPicture(Integer id,String articleLink,HttpServletRequest request) {
        if (id == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
        Map<Integer,String> map = this.getfilePaths(request);
        if (map.size() == CommonConst.NUMBER_0){
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        bannerService.updateById(id,articleLink,userService.findById(LoginUtil.getLoginUserId()).getAccount(),map);
        return ResponseUtil.success();
    }
    @ApiOperation("新增Banner")
    @PostMapping("/saveBanner")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "theShowEnd", value = "展示端 1 web 2 app", required = true),
            @ApiImplicitParam(name = "articleLink", value = "文章链接", required = true),
    })
    public ResponseEntity saveBanner(Integer theShowEnd,String articleLink, HttpServletRequest request){
        Map<Integer,String> map = this.getfilePaths(request);
        if (map.size() == CommonConst.NUMBER_0){
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        Banner banner = new Banner();
        banner.setTheShowEnd(theShowEnd);
        banner.setArticleLink(articleLink);
        banner.setHits(CommonConst.NUMBER_0);
        banner.setFirstMap(map.get(CommonConst.NUMBER_0));
        banner.setSecondMap(map.get(CommonConst.NUMBER_1));
        banner.setThirdlyMap(map.get(CommonConst.NUMBER_2));
        banner.setFourthlyMap(map.get(CommonConst.NUMBER_3));
        banner.setFifthMap(map.get(CommonConst.NUMBER_4));
        banner.setLastUpdatedBy(userService.findById(LoginUtil.getLoginUserId()).getAccount());
        bannerService.saveBanner(banner);
        return ResponseUtil.success();
    }

    @ApiOperation("删除Banner")
    @GetMapping("/deleteBanner")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", required = true),
    })
    public ResponseEntity deleteBanner(Integer id){
        if (id == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
        bannerService.deleteById(id);
        return ResponseUtil.success();
    }

    @ApiOperation("查找Banner")
    @GetMapping("/findByBannerList")
    public ResponseEntity findByBannerList(){
        return ResponseUtil.success(bannerService.findByBannerList());
    }

    private Map<Integer,String> getfilePaths(HttpServletRequest request){
        Map<Integer,String> map = new HashMap<>();
        try {
            request.setCharacterEncoding("utf-8"); //设置编码
            String realPath = request.getSession().getServletContext().getRealPath("/uploadFile/");
            File dir = new File(realPath);
            //文件目录不存在，就创建一个
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            StandardMultipartHttpServletRequest req = (StandardMultipartHttpServletRequest) request;
            //获取formdata的值
            Iterator<String> iterator = req.getFileNames();
            while (iterator.hasNext()) {
                List<MultipartFile> files = req.getFiles(iterator.next());
                if (files == null||files.size() >= CommonConst.NUMBER_6){
                    return map;
                }
                int limit = 0;
                for(MultipartFile file:files){
                    String fileUrl = UploadAndDownloadUtil.fileUpload(CommonUtil.getLocalPicPath(), file);
                    map.put(limit++,fileUrl);
                }
            }
        } catch (Exception e) {
            logger.error("处理图片出错", e);
        }
        return map;
    }
}