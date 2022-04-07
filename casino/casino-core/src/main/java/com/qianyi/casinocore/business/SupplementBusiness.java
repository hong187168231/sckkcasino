package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.ErrorOrder;
import com.qianyi.casinocore.repository.ErrorOrderRepository;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.vo.WmMemberTradeReportVo;
import com.qianyi.livewm.api.PublicWMApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class SupplementBusiness {

    @Autowired
    private PublicWMApi wmApi;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private ErrorOrderRepository errorOrderRepository;

    /**
     * 尝试3次补单
     *
     * @param errorOrder
     * @param thirdAccount
     */
    public void trySupplement(ErrorOrder errorOrder, String thirdAccount) {
        String orderNo = errorOrder.getOrderNo();
        int requestNum = 0;
        while (true) {
            try {
                //报表查询需间隔30秒，未搜寻到数据需间隔10秒。
                Thread.sleep(30 * 1000);
                if (requestNum >= 3) {
                    log.error("尝试3次补单失败,errorOrder={}",errorOrder.toString());
                    break;
                }
                requestNum++;
                PublicWMApi.ResponseEntity entity = wmApi.getMemberTradeReport(thirdAccount, null, orderNo, null, null, null);
                if (entity == null) {
                    log.error("查询WM交易记录时远程请求异常");
                    continue;
                }
                if (entity.getErrorCode() == 107) {
                    log.info("订单号:{}查询无记录", orderNo);
                    errorOrder.setStatus(1);
                    errorOrder.setRemark("自动补单成功,补单金额:0,订单查询无记录");
                    errorOrderRepository.save(errorOrder);
                    break;
                }
                if (entity.getErrorCode() != 0) {
                    log.info("订单号:{}查询交易记录异常,result={}", orderNo, entity.toString());
                    break;
                }
                String result = entity.getResult().toString();
                List<WmMemberTradeReportVo> records = JSON.parseArray(result, WmMemberTradeReportVo.class);

                for (WmMemberTradeReportVo vo : records) {
                    if (thirdAccount.equals(vo.getUser()) && orderNo.equals(vo.getOrdernum())) {
                        BigDecimal amount = new BigDecimal(vo.getMoney()).abs();
                        userMoneyService.addMoney(errorOrder.getUserId(), amount);
                        log.info("userMoney表金额补偿成功,userId={},money={}", errorOrder.getUserId(), amount);
                        errorOrder.setStatus(1);
                        errorOrder.setRemark("自动补单成功,补单金额:" + amount);
                        errorOrderRepository.save(errorOrder);
                        log.info("订单自动补单成功,errorOrder表更新成功,errorOrder={}", errorOrder.toString());
                        break;
                    }
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("查询WM交易记录时异常,msg={}", e.getMessage());
            }
        }
    }
}
