package com.qianyi.casinoadmin.controller;


import com.qianyi.casinocore.model.Banner;
import com.qianyi.casinocore.service.BannerService;
import com.qianyi.modulecommon.util.CommonConst;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/banner")
@Api(tags = "Banner设置")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @ApiOperation("修改Banner")
    @PostMapping("/updateBanner")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "banner主键", required = true),
            @ApiImplicitParam(name = "articleLink", value = "文章链接", required = true),
    })
    public String uploadPicture(Integer id,String articleLink,HttpServletRequest request) throws Exception {
        bannerService.updateById(id,articleLink,request);
        return CommonConst.SUCCESS;
    }
    @ApiOperation("新增Banner")
    @PostMapping("/saveBanner")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "banner", value = "banner对象", required = true),
    })
    public String saveBanner(Banner banner, HttpServletRequest request) throws Exception {
        bannerService.saveBanner(banner,request);
        return CommonConst.SUCCESS;
    }

    @ApiOperation("删除Banner")
    @GetMapping("/deleteBanner")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", required = true),
    })
    public String deleteBanner(Integer id) throws Exception {
        bannerService.deleteById(id);
        return CommonConst.SUCCESS;
    }

    @ApiOperation("查找Banner")
    @GetMapping("/findByBannerList")
    public List<Banner> findByBannerList() throws Exception {
        return bannerService.findByBannerList();
    }
}