package com.qianyi.casinoadmin.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.qianyi.casinoadmin.model.dto.VipReportDTO;
import com.qianyi.casinoadmin.model.dto.VipReportOtherProxyDTO;
import com.qianyi.casinoadmin.model.dto.VipReportProxyDTO;
import com.qianyi.casinoadmin.model.dto.VipReportTotalDTO;
import com.qianyi.casinoadmin.repository.ProxyVipMapper;
import com.qianyi.casinoadmin.util.PageBounds;
import com.qianyi.casinoadmin.util.PageResult;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.UserLevelRepository;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.LevelAwardVo;
import com.qianyi.casinocore.vo.LevelReportTotalVo;
import com.qianyi.casinocore.vo.VipReportVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class VipReportService {

    @Autowired
    ProxyUserService proxyUserService;
    @Autowired
    private ProxyVipMapper proxyVipMapper;

    @Autowired
    private UserService userService;

    public PageResult findVipByProxy(VipReportProxyDTO vipReportDTO, PageBounds pageBounds) {
        if (StrUtil.isBlank(vipReportDTO.getProxyUserName()) && ObjectUtil.isNull(vipReportDTO.getProxyUserId())) {
            Page<VipReportVo> list = proxyVipMapper.proxyZdList(vipReportDTO, pageBounds.toRowBounds());
            list.getResult().forEach(item -> {
                LevelAwardVo levelAwardVo = proxyVipMapper.userLevelInfo(item.getProxyUserId(), 1, vipReportDTO.getStartTime(), vipReportDTO.getEndTime());
                item.setProxyUserId(item.getProxyUserId());
                if (ObjectUtil.isNotNull(levelAwardVo)) {
                    item.setTodayAward(levelAwardVo.getTodayAward());
                    item.setRiseAward(levelAwardVo.getRiseAward());
                }
            });
            return PageResult.getPageResult(pageBounds, list);
        }
        return PageResult.getPageResult(pageBounds, new LinkedList());
    }


    public List<VipReportVo> findVipByProxy2(VipReportOtherProxyDTO vipReportDTO) {
        Integer proxyRole = null;
        if (StrUtil.isNotBlank(vipReportDTO.getProxyUserName())) {
            ProxyUser proxyUser = proxyUserService.findByUserName(vipReportDTO.getProxyUserName());
            proxyRole = proxyUser.getProxyRole();
        }
        if (proxyRole == 1) {
            List<VipReportVo> list = proxyVipMapper.proxyJdList(vipReportDTO);
            for (VipReportVo vipReportVo : list) {
                LevelAwardVo levelAwardVo = proxyVipMapper.userLevelInfo(vipReportVo.getProxyUserId(), 2, vipReportDTO.getStartTime(), vipReportDTO.getEndTime());
                vipReportVo.setProxyUserId(vipReportVo.getProxyUserId());
                vipReportVo.setTodayAward(levelAwardVo.getTodayAward());
                vipReportVo.setRiseAward(levelAwardVo.getRiseAward());
            }
            return list;
        }
        if (proxyRole == 2) {
            List<VipReportVo> list = proxyVipMapper.proxyQdList(vipReportDTO);
            for (VipReportVo vipReportVo : list) {
                LevelAwardVo levelAwardVo = proxyVipMapper.userLevelInfo(vipReportVo.getProxyUserId(), 3, vipReportDTO.getStartTime(), vipReportDTO.getEndTime());
                vipReportVo.setProxyUserId(vipReportVo.getProxyUserId());
                vipReportVo.setTodayAward(levelAwardVo.getTodayAward());
                vipReportVo.setRiseAward(levelAwardVo.getRiseAward());
            }
            return list;
        }
        return new LinkedList<>();
    }


    public PageResult findVipReport(VipReportDTO vipReportDTO) {
        PageBounds pageBounds = new PageBounds();
        pageBounds.setPageNo(vipReportDTO.getPageCode());
        pageBounds.setPageSize(vipReportDTO.getPageSize());
        if (StringUtils.hasLength(vipReportDTO.getAccount())) {
            User user = userService.findByAccount(vipReportDTO.getAccount());
            vipReportDTO.setUserId(user.getId());
        }
        Page<VipReportVo> userList = proxyVipMapper.userLevelList(vipReportDTO, pageBounds.toRowBounds());
        return PageResult.getPageResult(pageBounds, userList);
    }


    public LevelReportTotalVo findVipReportTotal(VipReportTotalDTO vipReportTotalDTO) {
        if (StrUtil.isNotBlank(vipReportTotalDTO.getLevelArray())) {
            vipReportTotalDTO.setPf("1");
        }
        if (ObjectUtil.isNotNull(vipReportTotalDTO.getUserId())) {
            vipReportTotalDTO.setPf("1");
        }
        LevelReportTotalVo levelTotalVo = proxyVipMapper.levelTotal(vipReportTotalDTO);
        return levelTotalVo;
    }


}