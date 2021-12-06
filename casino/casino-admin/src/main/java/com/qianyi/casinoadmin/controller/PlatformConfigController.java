package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.*;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.UploadAndDownloadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("platformConfig")
@Api(tags = "运营中心")
@Slf4j
public class PlatformConfigController {

    @Autowired
    private PlatformConfigService platformConfigService;

    @ApiOperation("玩家推广返佣配置查询")
    @GetMapping("/findCommission")
    public ResponseEntity<UserCommissionVo> findAll(){
        List<PlatformConfig> platformConfigList = platformConfigService.findAll();
        UserCommissionVo userCommissionVo = null;
        for (PlatformConfig platformConfig : platformConfigList) {
            userCommissionVo = UserCommissionVo.builder()
                    .name("玩家推广返佣配置")
                    .id(platformConfig.getId())
                    .firstCommission(platformConfig.getFirstCommission())
                    .secondCommission(platformConfig.getSecondCommission())
                    .thirdCommission(platformConfig.getThirdCommission())
                    .build();
        }
        return new ResponseEntity(ResponseCode.SUCCESS, userCommissionVo);
    }

    @ApiOperation("编辑玩家推广返佣配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "firstCommission", value = "一级代理返佣", required = true),
            @ApiImplicitParam(name = "secondCommission", value = "二级代理返佣", required = true),
            @ApiImplicitParam(name = "thirdCommission", value = "三级代理返佣", required = true)
    })
    @PostMapping("/updateCommission")
    public ResponseEntity<UserCommissionVo> update(BigDecimal firstCommission, BigDecimal secondCommission, BigDecimal thirdCommission){
        if (LoginUtil.checkNull(firstCommission,secondCommission,thirdCommission)){
            return ResponseUtil.custom("参数错误");
        }
//        BigDecimal commission = firstCommission.add(secondCommission).add(thirdCommission);
//        if(commission.compareTo(new BigDecimal(0.03)) >= 0){
//            return ResponseUtil.custom("代理返佣配置总和不能大于3%");
//        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if(!LoginUtil.checkNull(platformConfig)){
//            Date commissionUpdate = platformConfig.getCommissionUpdate();
//            if(commissionUpdate != null){
//                long time = new Date().getTime() - commissionUpdate.getTime();
//                int oneDay = 60 * 60 * 1000 * 24;//一天时间
//                if(oneDay > time){
//                    return ResponseUtil.custom("该配置，每24小时只能修改一次");
//                }
//            }
            platformConfig.setFirstCommission(firstCommission);
            platformConfig.setSecondCommission(secondCommission);
            platformConfig.setThirdCommission(thirdCommission);
            platformConfig.setCommissionUpdate(new Date());
            platformConfigService.save(platformConfig);
        }else{
            PlatformConfig platform = new PlatformConfig();
            platform.setFirstCommission(firstCommission);
            platform.setSecondCommission(secondCommission);
            platform.setThirdCommission(thirdCommission);
            platform.setCommissionUpdate(new Date());
            platformConfigService.save(platform);
        }
        return new ResponseEntity(ResponseCode.SUCCESS);
    }

    @ApiOperation("查询域名配置")
    @GetMapping("/findDomainName")
    public ResponseEntity<DomainNameVo> findDomainNameVo(){
        PlatformConfig first = platformConfigService.findFirst();
        DomainNameVo domainNameVo = new DomainNameVo();
        if (!LoginUtil.checkNull(first)){
            domainNameVo.setId(first.getId());
            domainNameVo.setName("域名配置");
            domainNameVo.setDomainNameConfiguration(first.getDomainNameConfiguration());
            domainNameVo.setProxyConfiguration(first.getProxyConfiguration());
        }
        return new ResponseEntity(ResponseCode.SUCCESS, domainNameVo);
    }

    @ApiOperation("编辑域名配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "domainNameConfiguration", value = "首页域名配置", required = true),
            @ApiImplicitParam(name = "proxyConfiguration", value = "推广注册域名配置", required = true),
    })
    @PostMapping("/updateDomainName")
    public ResponseEntity updateDomainName(Long id,String domainNameConfiguration, String proxyConfiguration){
        if (LoginUtil.checkNull(id,domainNameConfiguration, proxyConfiguration)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setId(id);
        first.setDomainNameConfiguration(domainNameConfiguration);
        first.setProxyConfiguration(proxyConfiguration);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }

    @ApiOperation("查询注册开关配置")
    @GetMapping("/findRegisterSwitch")
    public ResponseEntity<RegisterSwitchVo> findRegisterSwitchVo(){
        PlatformConfig first = platformConfigService.findFirst();
        RegisterSwitchVo registerSwitchVo = new RegisterSwitchVo();
        if (!LoginUtil.checkNull(first)){
            registerSwitchVo.setId(first.getId());
            registerSwitchVo.setName("注册开关");
            registerSwitchVo.setRegisterSwitch(first.getRegisterSwitch());
        }
        return new ResponseEntity(ResponseCode.SUCCESS, registerSwitchVo);
    }

    @ApiOperation("编辑注册开关配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true),
            @ApiImplicitParam(name = "registerSwitch", value = "注册开关配置", required = true),
    })
    @PostMapping("/updateRegisterSwitch")
    public ResponseEntity updateRegisterSwitch(Long id,Integer registerSwitch){
        if (LoginUtil.checkNull(id,registerSwitch)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setId(id);
        first.setRegisterSwitch(registerSwitch);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }


    /**
     * 短信风险警戒线查询
     * @return
     */
    @ApiOperation("短信余额警戒线查询")
    @GetMapping("/findMessageBalance")
    @NoAuthorization
    public ResponseEntity<PlatformConfig> findMessageBalance(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        SendMessageVo sendMessageVo = new SendMessageVo();
        BigDecimal sendMessageWarning = platformConfig.getSendMessageWarning();
        sendMessageVo.setSendMessageWarning(sendMessageWarning);
        return ResponseUtil.success(sendMessageVo);
    }

    /**
     * 编辑短信余额风险警戒线
     * @param sendMessageWarning 短信余额风险警戒值
     * @return
     */
    @ApiOperation("编辑短信余额风险警戒线")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sendMessageWarning", value = "短信余额风险警戒值", required = false),
    })
    @PostMapping("/updateMessageBalance")
    public ResponseEntity uodateMessageBalance(BigDecimal sendMessageWarning){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }

        if (!LoginUtil.checkNull(sendMessageWarning)){
            platformConfig.setSendMessageWarning(sendMessageWarning);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }

    @ApiOperation("查询人人代直属下级最大个数")
    @GetMapping("/findDirectly")
    public ResponseEntity<DirectlyVo> findDirectly(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        DirectlyVo directlyVo = new DirectlyVo();
        directlyVo.setName("人人代直属下级最大个数");
        directlyVo.setDirectlyUnderTheLower((platformConfig == null || platformConfig.getDirectlyUnderTheLower() ==null)? CommonConst.NUMBER_0:platformConfig.getDirectlyUnderTheLower());
        return ResponseUtil.success(directlyVo);
    }

    @ApiOperation("修改人人代直属下级最大个数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "directlyUnderTheLower", value = "人人代直属下级最大个数", required = true),
    })
    @PostMapping("/updateDirectly")
    public ResponseEntity updateDirectly(Integer directlyUnderTheLower){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        platformConfig.setDirectlyUnderTheLower(directlyUnderTheLower);
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }


    @ApiOperation("查询推广链接码")
    @GetMapping("/findPromotionCode")
    public ResponseEntity findPromotionCode(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getCompanyInviteCode());
    }

    @ApiOperation("修改推广链接码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "promotionCode", value = "推广链接", required = true),
    })
    @PostMapping("/updatePromotionCode")
    public ResponseEntity updatePromotionCode(String promotionCode){
        if (LoginUtil.checkNull(promotionCode)){
            return ResponseUtil.custom("参数必填");
        }
        String regex = "^[0-9a-zA-Z]{3,20}$";
        if (!promotionCode.matches(regex)){
            return ResponseUtil.custom("必须输入长度3-20位的数字或者字母");
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        platformConfig.setCompanyInviteCode(promotionCode);
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }

    @ApiOperation("查询web项目域名配置")
    @GetMapping("/findWebConfiguration")
    public ResponseEntity findWebConfiguration(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getWebConfiguration());
    }

    @ApiOperation("修改web项目域名配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "webConfiguration", value = "web项目域名配置", required = true),
    })
    @PostMapping("/updateWebConfiguration")
    public ResponseEntity updateWebConfiguration(String webConfiguration){
        if (LoginUtil.checkNull(webConfiguration)){
            return ResponseUtil.custom("参数必填");
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        platformConfig.setWebConfiguration(webConfiguration);
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }


    /**
     * 图片服务器地址查询
     * @return
     */
    @ApiOperation("图片服务器地址查询")
    @GetMapping("/findUploadUrl")
    public ResponseEntity findUploadUrl(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getUploadUrl());
    }

    /**
     * 修改图片服务器地址
     * @param uploadUrl 图片服务器地址
     * @return
     */
    @ApiOperation("修改图片服务器地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uploadUrl", value = "图片服务器地址", required = true),
    })
    @PostMapping("/updateUploadUrl")
    public ResponseEntity updateUploadUrl(String uploadUrl){
        if (LoginUtil.checkNull(uploadUrl)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setUploadUrl(uploadUrl);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }




    /**
     * 读取图片服务器地址查询
     * @return
     */
    @ApiOperation("访问图片服务器地址查询")
    @GetMapping("/findReadUploadUrl")
    public ResponseEntity findReadUploadUrl(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getReadUploadUrl());
    }

    /**
     * 修改读取图片服务器地址
     * @param uploadUrl 图片服务器地址
     * @return
     */
    @ApiOperation("修改访问图片服务器地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uploadUrl", value = "图片服务器地址", required = true),
    })
    @PostMapping("/updateReadUploadUrl")
    public ResponseEntity updateReadUploadUrl(String uploadUrl){
        if (LoginUtil.checkNull(uploadUrl)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setReadUploadUrl(uploadUrl);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }


    /**
     * 编辑logo图
     * @param file
     * @return
     */
    @ApiOperation("编辑logo图")
    @PostMapping(value = "/saveLogoPicture",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "编辑logo图")
    public ResponseEntity savePCPicture(@RequestPart(value = "file", required = false) MultipartFile file){
        PlatformConfig platformConfig= platformConfigService.findFirst();
        try {
            String uploadUrl = platformConfig.getUploadUrl();
            String fileUrl = UploadAndDownloadUtil.fileUpload(file,uploadUrl);
            platformConfig.setLogImageUrl(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }

    /**
     * logo图查询
     * @return
     */
    @ApiOperation("logo图查询")
    @GetMapping("/findLogoPicture")
    public ResponseEntity findLogoPicture(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getReadUploadUrl()+platformConfig.getLogImageUrl());
    }


    /**
     * 新增网站icon
     * @param file
     * @return
     */
    @NoAuthorization
    @ApiOperation("编辑网站icon")
    @PostMapping(value = "/saveWebsiteIcon",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "编辑网站icon")
    public ResponseEntity saveWebsiteIcon(@RequestPart(value = "file", required = false) MultipartFile file){
        PlatformConfig platformConfig= platformConfigService.findFirst();
        try {
            String uploadUrl = platformConfig.getUploadUrl();
            String fileUrl = UploadAndDownloadUtil.fileUpload(file,uploadUrl);
            platformConfig.setWebsiteIcon(fileUrl);
        } catch (Exception e) {
            return ResponseUtil.custom(CommonConst.PICTURENOTUP);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }


    /**
     * 网站icon查询
     * @return
     */
    @ApiOperation("网站icon查看")
    @GetMapping("/findWebsiteIcon")
    public ResponseEntity findWebsiteIcon(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getReadUploadUrl()+platformConfig.getWebsiteIcon());
    }


    /**
     * 修改金钱符号
     * @return
     */
    @ApiOperation("编辑金钱符号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "moneySymbol", value = "金钱符号", required = true),
    })
    @PostMapping("/updateMoneySymbol")
    public ResponseEntity updateMoneySymbol(String moneySymbol){
        if (LoginUtil.checkNull(moneySymbol)){
            return ResponseUtil.custom("参数错误");
        }
        PlatformConfig first = platformConfigService.findFirst();
        if (LoginUtil.checkNull(first)){
            first = new PlatformConfig();
        }
        first.setMoneySymbol(moneySymbol);
        platformConfigService.save(first);
        return ResponseUtil.success();
    }


    /**
     * 金钱符号查询
     * @return
     */
    @ApiOperation("金钱符号查询")
    @GetMapping("/findMoneySymbol")
    public ResponseEntity findMoneySymbol(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig==null?"":platformConfig.getMoneySymbol());
    }


}
