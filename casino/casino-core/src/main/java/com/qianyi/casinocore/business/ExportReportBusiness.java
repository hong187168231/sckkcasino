package com.qianyi.casinocore.business;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.qianyi.casinocore.model.ExportReport;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.ExportReportService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.vo.PersonReportVo;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportReportBusiness {

    @Autowired
    private ExportReportService exportReportService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    @Transactional
    public void comparison(String dayTime) {// dayTime为一天yyyy-MM-dd
        log.info("计算导出报表数据开始{}==============================================>",dayTime);
        long startLong = System.currentTimeMillis();
        exportReportService.deleteByOrderTimes(dayTime);
        String orderTimeStart = "'" + dayTime + "'";
        String orderTimeEnd = "'" + dayTime + "'";
        String startTime = dayTime + start;
        Date date = null;
        try {
            date = DateUtil.getDate(dayTime);
        } catch (ParseException e) {
            log.error("时间格式错误{}", dayTime);
            e.printStackTrace();
        }
        Calendar nowTime = Calendar.getInstance();
        nowTime.setTime(date);
        nowTime.add(Calendar.DATE, 1);
        String tomorrow = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        String endTime = tomorrow + end;
        List<PersonReportVo> reportResult = null;
        try {
            reportResult = userService.findMapExport("", startTime, endTime, "", orderTimeStart, orderTimeEnd, "");
        } catch (Exception e) {
            log.error("统计导出报表失败{}", e.getMessage());
        }
        List<ExportReport> exportReports = new ArrayList<>();

        List<ExportReport> exportReportList = new ArrayList<>();
        reportResult.forEach(personReportVo -> {
            ExportReport exportReport = new ExportReport();
            BeanUtils.copyProperties(personReportVo, exportReport);
            exportReport.setId(null);
            if (exportReport.check() || exportReport.getNum() >= CommonConst.NUMBER_1) {
                exportReport.setUserId(personReportVo.getId());
                try {
                    exportReport.setOrderTimes(DateUtil.getDate(dayTime));
                } catch (ParseException e) {
                    log.error("统计导出报表失败{}", e.getMessage());
                }
                exportReportList.add(exportReport);
            }
        });
        if (CollUtil.isNotEmpty(exportReportList)) {
            exportReports = getMap(exportReportList);
            Lists.partition(exportReports, 200).forEach(reports -> exportReportService.saveAll(reports));
        }
        log.info("计算导出报表数据结束{}耗时{}==============================================>",dayTime,System.currentTimeMillis()-startLong);
    }

    private List<ExportReport> getMap(List<ExportReport> content) {
        Set<String> ids = content.stream().filter(item -> StringUtils.hasLength(item.getThirdProxy()))
            .map(ExportReport::getThirdProxy).collect(Collectors.toSet());
        List<String> list = new ArrayList<>();
        list.addAll(ids);
        List<ProxyUser> proxyUsers = proxyUserService.findProxyUsers(list);
        Map<Long, ProxyUser> map = proxyUsers.stream().collect(Collectors.toMap(ProxyUser::getId, (p) -> p));
        for (ExportReport item : content) {
            if (!StringUtils.hasLength(item.getThirdProxy())) {
                continue;
            }
            ProxyUser proxyUser = map.get(Long.valueOf(item.getThirdProxy()));
            item.setThirdProxyName(proxyUser == null ? "" : proxyUser.getUserName());
        }
        return content;
    }
}
