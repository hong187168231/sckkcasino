package com.qianyi.casinoadmin.repository;


import com.github.pagehelper.Page;
import com.qianyi.casinoadmin.model.dto.VipReportDTO;
import com.qianyi.casinoadmin.model.dto.VipReportOtherProxyDTO;
import com.qianyi.casinoadmin.model.dto.VipReportProxyDTO;
import com.qianyi.casinoadmin.model.dto.VipReportTotalDTO;
import com.qianyi.casinocore.vo.LevelAwardVo;
import com.qianyi.casinocore.vo.LevelReportTotalVo;
import com.qianyi.casinocore.vo.VipReportVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@Mapper
public interface ProxyVipMapper {

    Page<VipReportVo> proxyZdList(VipReportProxyDTO dto, RowBounds rowBounds);

    List<VipReportVo> proxyJdList(VipReportOtherProxyDTO dto);

    List<VipReportVo> proxyQdList(VipReportOtherProxyDTO dto);

    LevelAwardVo userLevelInfo(Long proxyId, Integer proxyLevel,String startTime,String endTime);

    Page<VipReportVo> userLevelList(VipReportDTO dto, RowBounds rowBounds);

    LevelReportTotalVo levelTotal(VipReportTotalDTO dto);

}