package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.LunboPic;
import com.qianyi.casinocore.service.PictureService;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/picture")
@Api(tags = "公告管理")
public class PictureController {
    @Autowired
    private PictureService pictureService;
    @Autowired
    private SysUserService sysUserService;
    @ApiOperation("新增PC端轮播图")
    @PostMapping(value = "/savePCPicture",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "新增PC端轮播图")
    public ResponseEntity savePCPicture(@RequestPart(value = "file") MultipartFile file,@RequestParam(value = "文章链接") String remark,
                                        @RequestParam(value = "序号1-5") Integer no){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setTheShowEnd(CommonConst.NUMBER_1);//PC端 1
        return this.savePicture(file,remark,no,lunboPic);
    }
    @ApiOperation("新增移动端轮播图")
    @PostMapping(value = "/saveAppPicture",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "新增移动端轮播图")
    public ResponseEntity saveAppPicture(@RequestPart(value = "file") MultipartFile file,@RequestParam(value = "文章链接") String remark,
                                        @RequestParam(value = "序号1-5") Integer no){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setTheShowEnd(CommonConst.NUMBER_2);//APP端 2
        return this.savePicture(file,remark,no,lunboPic);
    }
    public ResponseEntity savePicture(MultipartFile file,String remark,Integer no,LunboPic lunboPic){
        if (file == null|| no == null||CommonUtil.checkNull(remark)){
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        lunboPic.setRemark(remark);
        lunboPic.setNo(no);
        lunboPic.setHits(CommonConst.NUMBER_0);
        try {
            String fileUrl = UploadAndDownloadUtil.fileUpload(CommonUtil.getLocalPicPath(), file);
            lunboPic.setUrl(fileUrl);
            String userName = sysUserService.findAllById(LoginUtil.getLoginUserId()).getUserName();
            lunboPic.setCreateBy(userName);
            lunboPic.setUpdateBy(userName);
            lunboPic.setCreateTime(new Date());
            lunboPic.setUpdateTime(new Date());
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        Specification<LunboPic> specification = new Specification<LunboPic>() {
            @Override
            public Predicate toPredicate(Root<LunboPic> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate;
                predicate=cb.equal(root.get("theShowEnd").as(String.class), lunboPic.getTheShowEnd());
                return predicate;
            }
        };
        return this.savePicture(specification,lunboPic);
    }
    private synchronized ResponseEntity savePicture(Specification<LunboPic> specification,LunboPic lunboPic){
        List<LunboPic> allPicture = pictureService.findAll(specification);
        if (allPicture != null && allPicture.size() >= CommonConst.NUMBER_5){
            return ResponseUtil.custom(CommonConst.TOOMANYPICTURESONTHECLIENT);
        }
        pictureService.save(lunboPic);
        return ResponseUtil.success();
    }
    @ApiOperation("修改Picture")
    @PostMapping(value = "/updatePicture",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "修改Picture")
    public ResponseEntity updatePicture(@RequestPart("file") MultipartFile file,@RequestParam(value = "id") Long id
            ,@RequestParam(value = "文章链接") String remark,@RequestParam(value = "序号1-5") Integer no) {
        if (id == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
        LunboPic lunboPic = pictureService.findLunboPicbyId(id);
        if (lunboPic == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
        lunboPic.setRemark(remark);
        lunboPic.setNo(no);
        try {
            String fileUrl = UploadAndDownloadUtil.fileUpload(CommonUtil.getLocalPicPath(), file);
            lunboPic.setUrl(fileUrl);
            lunboPic.setUpdateBy(sysUserService.findAllById(LoginUtil.getLoginUserId()).getUserName());
            lunboPic.setUpdateTime(new Date());
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        pictureService.save(lunboPic);
        return ResponseUtil.success();
    }
    @ApiOperation("删除picture")
    @GetMapping("/deletePicture")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", required = true),
    })
    public ResponseEntity deletePicture(Long id){
        if (id == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
        pictureService.deleteById(id);
        return ResponseUtil.success();
    }

    @ApiOperation("查找Banner")
    @GetMapping("/findByBannerList")
    public ResponseEntity findByBannerList(){
        return ResponseUtil.success(pictureService.findByLunboPicList());
    }
}
