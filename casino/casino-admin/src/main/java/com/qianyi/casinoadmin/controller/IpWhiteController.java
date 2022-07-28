package com.qianyi.casinoadmin.controller;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.IpWhite;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.IpWhiteService;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.IpWhiteVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("ipWhite")
@Api(tags = "系统管理")
@Slf4j
public class IpWhiteController {

    @Autowired
    private IpWhiteService ipWhiteService;

    @Autowired
    private SysUserService sysUserService;

    @ApiOperation("ip白名单分页查询")
    @GetMapping("/findPag")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "ip", value = "ip", required = false),})
    public ResponseEntity<IpWhiteVo> findIpBlackPag(Integer pageSize, Integer pageCode, String ip) {
        IpWhite ipBlack = new IpWhite();
        Sort sort = Sort.by("id").descending();
        ipBlack.setIp(ip);
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<IpWhite> ipWhitePage = ipWhiteService.findPage(ipBlack, pageable);
        PageResultVO<IpWhiteVo> pageResultVO = new PageResultVO(ipWhitePage);
        List<IpWhite> content = ipWhitePage.getContent();
        if (CollUtil.isNotEmpty(content)) {
            List<IpWhiteVo> ipWhiteVos = new LinkedList<>();
            Set<String> updateBys = content.stream().map(IpWhite::getUpdateBy).collect(Collectors.toSet());
            Set<String> createBys = content.stream().map(IpWhite::getCreateBy).collect(Collectors.toSet());
            updateBys.addAll(createBys);
            List<SysUser> sysUsers = sysUserService.findAll(new ArrayList<>(updateBys));
            Map<Long, String> maps = sysUsers.stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUserName, (key1, key2) -> key2));
            content.forEach(ipWhite -> {
                IpWhiteVo ipWhiteVo = new IpWhiteVo();
                ipWhiteVo.setId(ipWhite.getId());
                ipWhiteVo.setIp(ipWhite.getIp());
                ipWhiteVo.setRemark(ipWhite.getRemark());
                ipWhiteVo.setCreateTime(ipWhite.getCreateTime());
                ipWhiteVo.setUpdateTime(ipWhite.getUpdateTime());
                if (ipWhite.getCreateBy() != null) {
                    ipWhiteVo.setCreateBy(maps.get(Long.parseLong(ipWhite.getCreateBy())));
                }
                if (ipWhite.getUpdateBy() != null) {
                    ipWhiteVo.setUpdateBy(maps.get(Long.parseLong(ipWhite.getUpdateBy())));
                }
                ipWhiteVos.add(ipWhiteVo);
            });
            pageResultVO.setContent(ipWhiteVos);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("ip白名单添加")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ip", value = "ip", required = true),
        @ApiImplicitParam(name = "remark", value = "备注", required = false),
    })
    @PostMapping("save")
    public ResponseEntity saveUser(String ip, String remark){
        if (LoginUtil.checkNull(ip)) {
            return ResponseUtil.custom("参数不合法");
        }
        if (!ip.matches(RegexEnum.IP.getRegex())){
            return ResponseUtil.custom("IP地址不合法");
        }
        IpWhite byId = ipWhiteService.findByIpAndType(ip, CommonConst.NUMBER_1);
        if (!LoginUtil.checkNull(byId)) {
            return ResponseUtil.custom("数据重复");
        }
        IpWhite ipWhite = new IpWhite();
        ipWhite.setIp(ip);
        ipWhite.setType(CommonConst.NUMBER_1);
        ipWhite.setRemark(remark);
        try {
            ipWhiteService.save(ipWhite);
        }catch (Exception ex){
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        return ResponseUtil.success();
    }

    @ApiOperation("ip白名单编辑备注")
    @GetMapping("/updateRemark")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true),
        @ApiImplicitParam(name = "remark", value = "备注", required = true),})
    public ResponseEntity updateRemark(Long id, String remark) {
        IpWhite byId = ipWhiteService.findById(id);
        if (LoginUtil.checkNull(byId)) {
            return ResponseUtil.custom("找不到这个ip");
        }
        byId.setRemark(remark);
        ipWhiteService.save(byId);
        return ResponseUtil.success();
    }

    @GetMapping("/delete")
    @ApiOperation("ip白名单删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true),})
    public ResponseEntity delete(Long id) {
        IpWhite byId = ipWhiteService.findById(id);
        if (LoginUtil.checkNull(byId)) {
            return ResponseUtil.custom("找不到这个ip");
        }
        ipWhiteService.delete(byId);
        return ResponseUtil.success();
    }
}
