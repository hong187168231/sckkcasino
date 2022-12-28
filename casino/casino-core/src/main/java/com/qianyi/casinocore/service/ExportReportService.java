package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ExportReport;
import com.qianyi.casinocore.model.ProxyGameRecordReport;
import com.qianyi.casinocore.repository.ExportReportRepository;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.casinocore.util.SqlConst;
import com.qianyi.casinocore.vo.PersonReportVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

@Service
@Slf4j
public class ExportReportService {

    @Autowired
    private ExportReportRepository exportReportRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public void saveAll(List<ExportReport> exportReports) {
        exportReportRepository.saveAll(exportReports);
    }

    @Transactional
    public void deleteByOrderTimes(String startTime, String endTime) {
        exportReportRepository.deleteByOrderTimes(startTime, endTime);
    }

    @Transactional
    public void deleteByOrderTimes(String orderTimes) {
        exportReportRepository.deleteByOrderTimes(orderTimes);
    }

    @SuppressWarnings("unchecked")
    public List<PersonReportVo> findMapExport(String orderTimeStart, String orderTimeEnd,String sort,String proxy) throws Exception {
        String sql = MessageFormat.format(SqlConst.exportReportTotalSql, orderTimeStart, orderTimeEnd, sort,proxy);// 走报表
        List<String> list = PERSON_REPORT_VO_FIELD_LIST;
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        if (null != resultList && resultList.size() > 0) {
            List<Map<String, Object>> mapList = parsePersonReportMapList(resultList, list);
            return DTOUtil.map2DTO(mapList, PersonReportVo.class);
        } else {
            return null;
        }
    }

    private List<Map<String, Object>> parsePersonReportMapList(List<Object> resultList, List<String> listString) {
        List<Map<String, Object>> list = null;
        if (resultList != null && resultList.size() > CommonConst.NUMBER_0) {
            list = new LinkedList<>();

            for (Object result : resultList) {
                Map<String, Object> map = new HashMap<>();
                Object[] obj = (Object[]) result;
                for (int i = 0; i < listString.size(); i++) {
                    String field = listString.get(i);
                    Object value = obj[i];
                    map.put(field, value);
                }
                list.add(map);
            }
        }
        return list;
    }

    private static final List<String> PERSON_REPORT_VO_FIELD_LIST = Arrays
        .asList("account", "third_proxy", "id","third_proxy_name","num",
            "bet_amount", "validbet", "win_loss", "wash_amount", "service_charge", "all_profit_amount", "avg_benefit",
            "total_amount", "all_water", "todayAward", "riseAward");
}
