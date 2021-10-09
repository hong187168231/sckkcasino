package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.vo.ProxyUserBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Proxy;
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

    @Transactional
    public void processProxyUserBOList(List<ProxyUserBO> proxyUserBOList) {
        proxyUserBOList.forEach(item->{});
    }

    private void processItem(ProxyUserBO proxyUserBO){
        proxyReportBusiness.processUser(proxyUserBO);
        proxyDayReportBusiness.processUser(proxyUserBO);
    }

    private List<ProxyUserBO> getGroupUserNum(User user) {
        List<ProxyUserBO> proxyUserBOList = new ArrayList<>();
        if(user.getFirstPid()==null)
            proxyUserBOList.add(getProxyUser(user.getFirstPid(),user,true));

        return proxyUserBOList;
    }

    private ProxyUserBO getProxyUser(Long userId,User user, boolean isDirect) {
        ProxyUserBO proxyUserBO = new ProxyUserBO();
        proxyUserBO.setProxyUserId(userId);
        proxyUserBO.setDrect(isDirect);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        proxyUserBO.setDayTime(format.format(user.getCreateTime()));
        return proxyUserBO;
    }
}
