package com.qianyi.casinocore.business;


import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyDayReportService;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.RechargeRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class RechargeRecordBussiness {

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyDayReportService proxyDayReportService;

    @Autowired
    private ProxyReportService proxyReportService;

    /**
     * 内部调用，单线程
     * 统计新增金额，人数
     * @param rechargeRecordVo 充值消息对象
     */
    public void procerssShareProfit(RechargeRecordVo rechargeRecordVo){
        if(rechargeRecordVo == null || rechargeRecordVo.getUserId() == null || rechargeRecordVo.getFirstUserId() == null){
            return;
        }
        log.info("充值消息对象：【{}】", JSONObject.toJSON(rechargeRecordVo));
        //进行代理团队数据操作
        setProxyDayData(rechargeRecordVo);
    }

    @Transactional
    public void setProxyDayData(RechargeRecordVo rechargeRecordVo) {
        User user = userService.findById(rechargeRecordVo.getUserId());
        if(user == null){
            return;
        }
        //直属代理数据统计
        this.setUserAgent(rechargeRecordVo, rechargeRecordVo.getFirstUserId());
        Boolean flag = false;
        //是否首冲
        if(rechargeRecordVo.getIsFirst() != null && rechargeRecordVo.getIsFirst() == 0){
            flag = true;
            setProxyReport(rechargeRecordVo.getFirstUserId(), true);
        }

        //第二层级代理
        if(rechargeRecordVo.getSecondUserId() != null){
            this.setUserAgent(rechargeRecordVo, rechargeRecordVo.getSecondUserId());
        }else{
            return;
        }
        if(flag){
            setProxyReport(rechargeRecordVo.getSecondUserId(), false);
        }

        //第三层级代理
        if(rechargeRecordVo.getThirdUserId() != null){
            this.setUserAgent(rechargeRecordVo, rechargeRecordVo.getThirdUserId());
        }else{
            return;
        }
        if(flag){
            setProxyReport(rechargeRecordVo.getThirdUserId(), false);
        }
    }

    /**
     * 首冲需要添加团队充值人数
     */
    private void setProxyReport(Long userId, Boolean flag) {
        ProxyReport proxyReport = proxyReportService.findByUserId(userId);
        if(proxyReport == null){
            proxyReport = new ProxyReport();
            proxyReport.setAllGroupNum(1);
            if(flag){
                proxyReport.setDirectGroupNum(1);
            }else{
                proxyReport.setOtherGroupNum(1);
            }
        }else{
            proxyReport.setAllGroupNum(proxyReport.getAllGroupNum() + 1);
            if(flag){
                proxyReport.setDirectGroupNum(proxyReport.getDirectGroupNum() + 1);
            }else{
                proxyReport.setOtherGroupNum(proxyReport.getOtherGroupNum() + 1);
            }
        }
        log.info("首冲添加人数：【{}】", proxyReport);
        proxyReportService.save(proxyReport);
    }

    /**
     * 直属代理数据统计
     * @param rechargeRecordVo
     */
    private void setUserAgent(RechargeRecordVo rechargeRecordVo, Long userId) {
        //得到当天的日期
        //得到直属代理的数据
        User firstUser = userService.findById(userId);
        if(firstUser == null){
            return;
        }
        //设置直属代理的数据
        setProxyDate(rechargeRecordVo, userId);
    }


    private void setProxyDate(RechargeRecordVo rechargeRecordVo, Long userId) {
        ProxyDayReport Proxy = proxyDayReportService.findByUserIdAndDay(userId, getTodayDate());
        if(Proxy == null){
            Proxy = new ProxyDayReport();
            //存款
            Proxy.setDeppositeAmount(rechargeRecordVo.getChargeAmount());
            //存款人数
            Proxy.setNewNum(1);
            //日期
            Proxy.setDayTime(getTodayDate());
        }else{
            Proxy.setDeppositeAmount(Proxy.getDeppositeAmount().add(rechargeRecordVo.getChargeAmount()));
            Proxy.setNewNum(Proxy.getNewNum() + 1);
        }
        log.info("代理数据：【{}】", Proxy);
        proxyDayReportService.save(Proxy);
    }

    /**
     * 取当天日期数据
     *
     * @return
     */
    private String getTodayDate() {
        SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd");
        String today = smf.format(new Date());
        return today;
    }

}
