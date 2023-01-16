package com.qianyi.casinoadmin.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.qianyi.casinoadmin.model.dto.*;
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
import com.qianyi.casinocore.vo.VipProxyReportVo;
import com.qianyi.casinocore.vo.VipReportVo;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
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
            Page<VipProxyReportVo> list = proxyVipMapper.proxyZdList(vipReportDTO, pageBounds.toRowBounds());
            list.getResult().forEach(item -> {
                String startTime = vipReportDTO.getStartTime().substring(0,10);
                String endTime = vipReportDTO.getEndTime().substring(0,10);
                LevelAwardVo levelAwardVo = proxyVipMapper.userLevelInfo(item.getProxyUserId(),
                        1, startTime, endTime);
                item.setProxyUserId(item.getProxyUserId());
                if (ObjectUtil.isNotNull(levelAwardVo)) {
                    item.setTodayAward(levelAwardVo.getTodayAward());
                    item.setRiseAward(levelAwardVo.getRiseAward());
                    item.setAwardAmount(item.getTodayAward().add(item.getRiseAward()));
                    item.setValidBet(levelAwardVo.getBetAmount());
                    item.setWinLoss(levelAwardVo.getWinLoss());

                }
            });
            return PageResult.getPageResult(pageBounds, list);
        }
        if (StrUtil.isNotBlank(vipReportDTO.getProxyUserName())) {
            ProxyUser proxyUser = proxyUserService.findByUserName(vipReportDTO.getProxyUserName());
            Integer proxyRole = proxyUser.getProxyRole();
            LevelAwardVo levelAwardVo = proxyVipMapper.userLevelInfo(proxyUser.getId(), proxyRole, vipReportDTO.getStartTime(), vipReportDTO.getEndTime());
            List<VipProxyReportVo> list = new ArrayList<>();
            VipProxyReportVo vo = new VipProxyReportVo();
            vo.setProxyUsersNum(proxyUser.getProxyUsersNum());
            vo.setProxyUserId(proxyUser.getId());
            vo.setUserName(proxyUser.getUserName());
            if (ObjectUtil.isNotNull(levelAwardVo)) {
                vo.setTodayAward(levelAwardVo.getTodayAward());
                vo.setRiseAward(levelAwardVo.getRiseAward());
                vo.setAwardAmount(vo.getTodayAward().add(vo.getRiseAward()));
                vo.setValidBet(levelAwardVo.getBetAmount());
                vo.setWinLoss(levelAwardVo.getWinLoss());
            }
            list.add(vo);
            return PageResult.getPageResult(pageBounds, list);
        }
        return PageResult.getPageResult(pageBounds, new LinkedList());
    }


    public List<VipProxyReportVo> findVipByProxy2(VipReportOtherProxyDTO vipReportDTO) {
        Integer proxyRole = null;
        ProxyUser proxyUser = null;
        if (StrUtil.isNotBlank(vipReportDTO.getProxyUserName())) {
            proxyUser = proxyUserService.findByUserName(vipReportDTO.getProxyUserName());
            if (ObjectUtil.isNull(proxyUser)) {
                return new LinkedList<>();
            }
            proxyRole = proxyUser.getProxyRole();
        }
        if (proxyRole == 1) {
            vipReportDTO.setFirstProxyId(proxyUser.getId());
            List<VipProxyReportVo> list = proxyVipMapper.proxyJdList(vipReportDTO);
            for (VipProxyReportVo vipReportVo : list) {
                LevelAwardVo levelAwardVo = proxyVipMapper.userLevelInfo(vipReportVo.getProxyUserId(),
                        2, vipReportDTO.getStartTime(), vipReportDTO.getEndTime());
                vipReportVo.setProxyUserId(vipReportVo.getId());
                if (ObjectUtil.isNotNull(levelAwardVo)) {
                    vipReportVo.setTodayAward(levelAwardVo.getTodayAward());
                    vipReportVo.setRiseAward(levelAwardVo.getRiseAward());
                    vipReportVo.setAwardAmount(vipReportVo.getTodayAward().add(vipReportVo.getRiseAward()));
                    vipReportVo.setValidBet(levelAwardVo.getBetAmount());
                    vipReportVo.setWinLoss(levelAwardVo.getWinLoss());
                }

            }
            return list;
        }
        if (proxyRole == 2) {
            vipReportDTO.setSecondProxyId(proxyUser.getId());
            List<VipProxyReportVo> list = proxyVipMapper.proxyQdList(vipReportDTO);
            for (VipProxyReportVo vipReportVo : list) {
                LevelAwardVo levelAwardVo = proxyVipMapper.userLevelInfo(vipReportVo.getProxyUserId(),
                        3, vipReportDTO.getStartTime(), vipReportDTO.getEndTime());
                vipReportVo.setProxyUserId(vipReportVo.getId());
                if (ObjectUtil.isNotNull(levelAwardVo)) {
                    vipReportVo.setTodayAward(levelAwardVo.getTodayAward());
                    vipReportVo.setRiseAward(levelAwardVo.getRiseAward());
                    vipReportVo.setAwardAmount(vipReportVo.getTodayAward().add(vipReportVo.getRiseAward()));
                    vipReportVo.setValidBet(levelAwardVo.getBetAmount());
                    vipReportVo.setWinLoss(levelAwardVo.getWinLoss());
                }
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


    public LevelReportTotalVo findProxyVipReportTotal(VipProxyReportTotalDTO vipReportTotalDTO) {
        LevelReportTotalVo levelReportTotalVo;
        if (StrUtil.isNotBlank(vipReportTotalDTO.getAccount())) {
            ProxyUser proxyUser = proxyUserService.findByUserName(vipReportTotalDTO.getAccount());
            Integer proxyRole = proxyUser.getProxyRole();
            vipReportTotalDTO.setProxyLevel(proxyRole);
            levelReportTotalVo = proxyVipMapper.levelProxyTotal(vipReportTotalDTO);
        } else {
            levelReportTotalVo = proxyVipMapper.levelProxyTotal(vipReportTotalDTO);
        }
        levelReportTotalVo.setAwardAmount(levelReportTotalVo.getTodayAward().add(levelReportTotalVo.getRiseAward()));
        return levelReportTotalVo;
    }


}