package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.LunboPic;
import com.qianyi.casinocore.model.WithdrawOrder;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
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

    private static List PCNo = new ArrayList();
    private static List AppNo = new ArrayList();
    static {
        PCNo.add(CommonConst.NUMBER_6);
        PCNo.add(CommonConst.NUMBER_7);
        PCNo.add(CommonConst.NUMBER_8);
        PCNo.add(CommonConst.NUMBER_9);
        PCNo.add(CommonConst.NUMBER_10);
        AppNo.add(CommonConst.NUMBER_1);
        AppNo.add(CommonConst.NUMBER_2);
        AppNo.add(CommonConst.NUMBER_3);
        AppNo.add(CommonConst.NUMBER_4);
        AppNo.add(CommonConst.NUMBER_5);
    }
    @ApiOperation("新增PC端轮播图")
    @PostMapping(value = "/savePCPicture",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "新增PC端轮播图")
    public ResponseEntity savePCPicture(@RequestPart(value = "file") MultipartFile file,@RequestParam(value = "序号6-10") Integer no){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setTheShowEnd(CommonConst.NUMBER_1);//PC端 1
        if (!PCNo.contains(no)){
            return ResponseUtil.custom("序号只能设置6-10");
        }
        return this.savePicture(file,no,lunboPic);
    }
    @ApiOperation("新增移动端轮播图")
    @PostMapping(value = "/saveAppPicture",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "新增移动端轮播图")
    public ResponseEntity saveAppPicture(@RequestPart(value = "file") MultipartFile file,@RequestParam(value = "序号1-5") Integer no){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setTheShowEnd(CommonConst.NUMBER_2);//APP端 2
        if (!AppNo.contains(no)){
            return ResponseUtil.custom("序号只能设置1-5");
        }
        return this.savePicture(file,no,lunboPic);
    }
    public ResponseEntity savePicture(MultipartFile file,Integer no,LunboPic lunboPic){
        if (file == null){
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        lunboPic.setId(no.longValue());
        lunboPic.setNo(no);
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
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                list.add(cb.equal(root.get("theShowEnd").as(String.class),lunboPic.getTheShowEnd()));
                list.add(cb.equal(root.get("no").as(Integer.class), lunboPic.getNo()));
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return this.savePicture(specification,lunboPic);
    }
    private synchronized ResponseEntity savePicture(Specification<LunboPic> specification,LunboPic lunboPic){
        List<LunboPic> allPicture = pictureService.findAll(specification);
        if (allPicture != null && allPicture.size() != CommonConst.NUMBER_0){
            return ResponseUtil.custom(CommonConst.TOOMANYPICTURESONTHECLIENT);
        }
        pictureService.save(lunboPic);
        return ResponseUtil.success();
    }
    @ApiOperation("修改Picture")
    @PostMapping(value = "/updatePicture",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "修改Picture")
    public ResponseEntity updatePicture(@RequestPart("file") MultipartFile file,@RequestParam(value = "id") Long id) {
        if (id == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
        LunboPic lunboPic = pictureService.findLunboPicbyId(id);
        if (lunboPic == null){
            return ResponseUtil.custom(CommonConst.IDNOTNULL);
        }
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
//    @ApiOperation("删除picture")
//    @GetMapping("/deletePicture")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "主键id", required = true),
//    })
//    public ResponseEntity deletePicture(Long id){
//        if (id == null){
//            return ResponseUtil.custom(CommonConst.IDNOTNULL);
//        }
//        pictureService.deleteById(id);
//        return ResponseUtil.success();
//    }

    @ApiOperation("查找Banner")
    @GetMapping("/findByBannerList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "theShowEnd", value = "1 web 2 app", required = false),
    })
    public ResponseEntity findByBannerList(Integer theShowEnd){
        Specification<LunboPic> condition = this.getCondition(theShowEnd);
        Sort sort=Sort.by("no").ascending();
        return ResponseUtil.success(pictureService.findByLunboPicList(condition,sort));
    }

    private Specification<LunboPic> getCondition(Integer theShowEnd) {
        Specification<LunboPic> specification = new Specification<LunboPic>() {
            List<Predicate> list = new ArrayList<Predicate>();
            @Override
            public Predicate toPredicate(Root<LunboPic> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                if (theShowEnd != null) {
                    list.add(cb.equal(root.get("theShowEnd").as(Integer.class), theShowEnd));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}
