package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyDayReportService;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.vo.ProxyUserBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserGroupNumBusiness {

    @Autowired
    private ProxyDayReportBusiness proxyDayReportBusiness;

    @Autowired
    private ProxyReportBusiness proxyReportBusiness;

    @Autowired
    private ProxyDayReportService proxyDayReportService;

    @Autowired
    private ProxyReportService proxyReportService;

    /**
     * 内部调用，单线程
     * 统计新增金额，人数
     */
    public void processUser(User user){
        log.info("新增的用户对象：{}", user);
        if(user == null){
            return;
        }

        List<ProxyUserBO> proxyUserBOList = getGroupUserNum(user);
        processProxyUserBOList(proxyUserBOList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void processProxyUserBOList(List<ProxyUserBO> proxyUserBOList) {
        List<ProxyDayReport> proxyDayReportList = new ArrayList<>();
        List<ProxyReport> proxyReportList = new ArrayList<>();
        proxyUserBOList.forEach(item->processItem(item,proxyDayReportList,proxyReportList));
        proxyDayReportService.saveAll(proxyDayReportList);
        proxyReportService.saveAll(proxyReportList);
    }

    private void processItem(ProxyUserBO proxyUserBO,List<ProxyDayReport> proxyDayReportList,List<ProxyReport> proxyReportList){
        log.info("process proxy user BO item");
        proxyReportList.add(proxyReportBusiness.processUser(proxyUserBO));
        proxyDayReportList.add(proxyDayReportBusiness.processUser(proxyUserBO));
    }

    private List<ProxyUserBO> getGroupUserNum(User user) {
        List<ProxyUserBO> proxyUserBOList = new ArrayList<>();
        if(user.getFirstPid()!=null)
            proxyUserBOList.add(getProxyUser(user.getFirstPid(),user,true));
        if(user.getSecondPid()!=null)
            proxyUserBOList.add(getProxyUser(user.getSecondPid(),user,true));
        if(user.getThirdPid()!=null)
            proxyUserBOList.add(getProxyUser(user.getThirdPid(),user,true));
        log.info("get list BO {}",proxyUserBOList);
        return proxyUserBOList;
    }

    private ProxyUserBO getProxyUser(Long userId,User user, boolean isDirect) {;
        ProxyUserBO proxyUserBO = new ProxyUserBO();
        proxyUserBO.setProxyUserId(userId);
        proxyUserBO.setDrect(isDirect);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        proxyUserBO.setDayTime(format.format(user.getCreateTime()));
        log.info("generate bo is {}",proxyUserBO);
        return proxyUserBO;
    }
}
