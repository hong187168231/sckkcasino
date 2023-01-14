package com.qianyi.casinoadmin.repository;


import com.github.pagehelper.Page;
import com.qianyi.casinoadmin.model.dto.*;
import com.qianyi.casinocore.vo.LevelAwardVo;
import com.qianyi.casinocore.vo.LevelReportTotalVo;
import com.qianyi.casinocore.vo.VipProxyReportVo;
import com.qianyi.casinocore.vo.VipReportVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@Mapper
public interface ProxyVipMapper {

    Page<VipProxyReportVo> proxyZdList(VipReportProxyDTO dto, RowBounds rowBounds);

    List<VipProxyReportVo> proxyJdList(VipReportOtherProxyDTO dto);

    List<VipProxyReportVo> proxyQdList(VipReportOtherProxyDTO dto);

    LevelAwardVo userLevelInfo(Long proxyId, Integer proxyLevel,String startTime,String endTime);

    Page<VipReportVo> userLevelList(VipReportDTO dto, RowBounds rowBounds);

    LevelReportTotalVo levelTotal(VipReportTotalDTO dto);

    LevelReportTotalVo levelProxyTotal(VipProxyReportTotalDTO dto);

}