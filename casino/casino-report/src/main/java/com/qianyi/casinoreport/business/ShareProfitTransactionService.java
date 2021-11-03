package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ShareProfitTransactionService {

    @Autowired
    private ShareprofitItemService sharepointItemService;

    @Autowired
    private ProxyReportService proxyReportService;

    @Autowired
    private ProxyDayReportService proxyDayReportService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private GameRecordService gameRecordService;

    @Transactional(rollbackFor = Exception.class)
    public void processShareProfitList(List<ShareProfitBO> shareProfitBOList, GameRecord record) {
        List<ProxyDayReport> proxyDayReportList = new ArrayList<>();
        List<ProxyReport> proxyReportList = new ArrayList<>();
        List<UserMoney> userMoneyList = new ArrayList<>();
        List<ShareProfitChange> shareProfitChangeList = new ArrayList<>();

        shareProfitBOList.forEach(item-> sharepointItemService.processItem(item,record,proxyDayReportList,proxyReportList,userMoneyList,shareProfitChangeList));
        proxyDayReportService.saveAll(proxyDayReportList);
        proxyReportService.saveAll(proxyReportList);
        userMoneyList.forEach(item->userMoneyService.changeProfit(item.getUserId(),item.getShareProfit()));
        shareProfitChangeService.saveAll(shareProfitChangeList);
//        int i = 1/0;
        updateShareProfitStatus(record);
    }

    /**
     * 更新分润状态
     *
     * @param record
     */
    public void updateShareProfitStatus(GameRecord record) {
        record.setShareProfitStatus(Constants.yes);
        gameRecordService.save(record);
    }
}
