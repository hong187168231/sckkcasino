package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.ErrorOrder;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.repository.ErrorOrderRepository;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinocore.vo.TransactionDetailDmc;
import com.qianyi.casinocore.vo.TransferVo;
import com.qianyi.casinocore.vo.WmMemberTradeReportVo;
import com.qianyi.liveae.api.PublicAeApi;
import com.qianyi.livedg.api.DgApi;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.lottery.api.LotteryDmcApi;
import com.qianyi.lottery.api.PublicLotteryApi;
import com.qianyi.modulecommon.executor.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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
    private PublicGoldenFApi goldenFApi;
    @Autowired
    private PublicAeApi aeApi;
    @Autowired
    private PublicLotteryApi lotteryApi;
    @Autowired
    private ErrorOrderRepository errorOrderRepository;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;

    @Autowired
    private LotteryDmcApi lotteryDmcApi;

    @Autowired
    private DgApi dgApi;

    /**
     * 尝试3次补单
     *
     * @param errorOrder
     * @param thirdAccount
     */
    public void tryWMSupplement(ErrorOrder errorOrder, String thirdAccount) {
        String orderNo = errorOrder.getOrderNo();
        log.info("开始补单userId:{},{}",errorOrder.getUserId(),orderNo);
        int requestNum = 0;
        while (true) {
            try {
                if (requestNum >= 3) {
                    log.error("WM尝试3次补单失败,errorOrder={}", errorOrder.toString());
                    break;
                }
                requestNum++;
                //报表查询需间隔30秒，未搜寻到数据需间隔10秒。
                Thread.sleep(30 * 1000);
                PublicWMApi.ResponseEntity entity = wmApi.getMemberTradeReport(thirdAccount, null, orderNo, null, null, null);
                if (entity == null) {
                    log.error("查询WM交易记录时远程请求异常");
                    continue;
                }
                if (entity.getErrorCode() == 107) {
                    log.info("订单号:{}查询无记录", orderNo);
                    //转入wm时，wm查询无记录说明wm加点失败，要把本地的钱加回来，
                    if (errorOrder.getType() == AccountChangeEnum.WM_IN.getType()) {
                        //更新错误订单表状态
                        Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + errorOrder.getMoney().stripTrailingZeros().toPlainString() + ",转入WM时加点失败,加回本地额度");
                        if (count > 0) {
                            //加回额度
                            addMoney(errorOrder.getUserId(), errorOrder.getMoney());
                            //记录账变
                            saveAccountChange(errorOrder, errorOrder.getMoney());
                        }
                    } else if (errorOrder.getType() == AccountChangeEnum.RECOVERY.getType()) {
                        //转出wm时，是先扣减wm的钱再加回本地，wm查询无记录说明没有扣点成功，本地也不用把钱加回来,更新状态就行
                        updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出WM时扣点失败,额度未丢失");
                    }
                    break;
                }
                if (entity.getErrorCode() != 0) {
                    log.info("订单号:{}查询交易记录异常,result={}", orderNo, entity.toString());
                    continue;
                }
                if (entity.getErrorCode() == 0) {
                    String result = entity.getResult().toString();
                    List<WmMemberTradeReportVo> records = JSON.parseArray(result, WmMemberTradeReportVo.class);
                    for (WmMemberTradeReportVo vo : records) {
                        if (thirdAccount.equals(vo.getUser()) && orderNo.equals(vo.getOrdernum())) {
                            //转入wm,本地先扣减，确认三方加点成功无需加回本地余额
                            if (vo.getOp_code() == 121 && errorOrder.getType() == AccountChangeEnum.WM_IN.getType()) {
                                updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转入WM时加点成功,额度未丢失");
                                //转出WM时，三方先扣减，确认三方扣点成功加回本地余额
                            } else if (vo.getOp_code() == 122 && errorOrder.getType() == AccountChangeEnum.RECOVERY.getType()) {
                                //以WM额度为准
                                BigDecimal money = new BigDecimal(vo.getMoney()).abs();
                                //更新错误订单表状态
                                Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + money.stripTrailingZeros().toPlainString() + ",转出WM时扣点成功,加回本地额度");
                                if (count > 0) {
                                    //加回额度
                                    addMoney(errorOrder.getUserId(), money);
                                    //记录账变
                                    saveAccountChange(errorOrder, money);
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("查询WM交易记录时异常,msg={}", e.getMessage());
            }
        }
    }


    public void tryGoldenFSupplement(ErrorOrder errorOrder, String thirdAccount, String walletCode) {
        String orderNo = errorOrder.getOrderNo();
        int requestNum = 0;
        while (true) {
            try {
                if (requestNum >= 3) {
                    log.error("goldenf尝试3次补单失败,errorOrder={}", errorOrder.toString());
                    break;
                }
                requestNum++;
                Thread.sleep(30 * 1000);
                long time = System.currentTimeMillis();
                PublicGoldenFApi.ResponseEntity playerTransactionRecord = goldenFApi.getPlayerTransactionRecord(thirdAccount, time, time, walletCode, orderNo, null);
                if (playerTransactionRecord == null) {
                    continue;
                }
                String errorCode = playerTransactionRecord.getErrorCode();
                if (!ObjectUtils.isEmpty(errorCode)) {
                    //查询无记录
                    if ("9402".equals(errorCode)) {
                        Integer orderType = errorOrder.getType();
                        //转入goldenF时，goldenF查询无记录说明goldenF加点失败，要把本地的钱加回来，
                        if (orderType == AccountChangeEnum.PG_CQ9_IN.getType() || orderType == AccountChangeEnum.SABASPORT_IN.getType()) {
                            //更新错误订单表状态
                            Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + errorOrder.getMoney().stripTrailingZeros().toPlainString() + ",转入" + errorOrder.getPlatform() + "时加点失败,加回本地额度");
                            if (count > 0) {
                                //加回额度
                                addMoney(errorOrder.getUserId(), errorOrder.getMoney());
                                //记录账变
                                saveAccountChange(errorOrder, errorOrder.getMoney());
                            }
                        } else if (orderType == AccountChangeEnum.PG_CQ9_OUT.getType() || orderType == AccountChangeEnum.SABASPORT_OUT.getType()) {
                            //转出goldenF时，是先扣减goldenF的钱再加回本地，goldenF查询无记录说明没有扣点成功，本地也不用把钱加回来,更新状态就行
                            updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出" + errorOrder.getPlatform() + "时扣点失败,额度未丢失");
                        }
                        break;
                    } else {
                        log.error("goldenf远程确认转账记录异常，errorOrder:{},playerTransactionRecord={}", errorOrder.toString(), playerTransactionRecord.toString());
                        continue;
                    }
                }
                JSONObject jsonData = JSONObject.parseObject(playerTransactionRecord.getData());
                JSONArray translogs = jsonData.getJSONArray("translogs");
                //查询有记录
                if (translogs.size() > 0) {
                    Integer orderType = errorOrder.getType();
                    //转入goldenF时，goldenF查询有记录说明goldenF加点成功，无需加回本地余额
                    if (orderType == AccountChangeEnum.PG_CQ9_IN.getType() || orderType == AccountChangeEnum.SABASPORT_IN.getType()) {
                        //更新错误订单表状态
                        updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转入" + errorOrder.getPlatform() + "时加点成功,额度未丢失");
                    } else if (orderType == AccountChangeEnum.PG_CQ9_OUT.getType() || orderType == AccountChangeEnum.SABASPORT_OUT.getType()) {
                        //转出goldenF时，是先扣减goldenF的钱再加回本地，goldenF查询有记录说明扣点成功，本地的把钱加回来
                        BigDecimal money = errorOrder.getMoney();
                        //更新错误订单表状态
                        Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + money.stripTrailingZeros().toPlainString() + ",转出" + errorOrder.getPlatform() + "时扣点成功,加回本地额度");
                        if (count > 0) {
                            //加回额度
                            addMoney(errorOrder.getUserId(), money);
                            //记录账变
                            saveAccountChange(errorOrder, money);
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("查询goldenf交易记录时异常,msg={}", e.getMessage());
            }
        }
    }

    public void tryAeSupplement(ErrorOrder errorOrder, String thirdAccount) {
        String orderNo = errorOrder.getOrderNo();
        int requestNum = 0;
        while (true) {
            try {
                if (requestNum >= 3) {
                    log.error("AE尝试3次补单失败,errorOrder={}", errorOrder.toString());
                    break;
                }
                requestNum++;
                Thread.sleep(30 * 1000);
                JSONObject result = aeApi.checkTransferOperation(orderNo);
                if (result == null) {
                    log.error("查询AE交易记录时远程请求异常");
                    continue;
                }
                String status = result.getString("status");
                BigDecimal money = errorOrder.getMoney();
                Integer orderType = errorOrder.getType();
                if ("1017".equals(status)) {
                    log.info("AE订单号:{}查询无记录", orderNo);
                    //转入AE时，AE查询无记录说明AE加点失败，要把本地的钱加回来，
                    if (orderType == AccountChangeEnum.AE_IN.getType()) {
                        //更新错误订单表状态
                        Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + money.stripTrailingZeros().toPlainString() + ",转入AE时加点失败,加回本地额度");
                        if (count > 0) {
                            //加回额度
                            addMoney(errorOrder.getUserId(), money);
                            //记录账变
                            saveAccountChange(errorOrder, money);
                        }
                    } else if (orderType == AccountChangeEnum.AE_OUT.getType()) {
                        //转出AE时，是先扣减AE的钱再加回本地，AE查询无记录说明没有扣点成功，本地也不用把钱加回来,更新状态就行
                        updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出AE时扣点失败,额度未丢失");
                    }
                    break;
                }
                if (!PublicAeApi.SUCCESS_CODE.equals(status)) {
                    log.info("AE订单号:{}查询交易记录异常,result={}", orderNo, result);
                    continue;
                }
                if (PublicAeApi.SUCCESS_CODE.equals(status)) {
                    Integer txStatus = result.getInteger("txStatus");
                    if (txStatus == 1) {
                        //转入AE,本地先扣减，确认三方加点成功无需加回本地余额
                        if (orderType == AccountChangeEnum.AE_IN.getType()) {
                            updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转入AE时加点成功,额度未丢失");
                        } else if (orderType == AccountChangeEnum.AE_OUT.getType()) {
                            //转出AE时，三方先扣减，确认三方扣点成功加回本地余额
                            Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + money.stripTrailingZeros().toPlainString() + ",转出AE时扣点成功,加回本地额度");
                            if (count > 0) {
                                //加回额度
                                addMoney(errorOrder.getUserId(), money);
                                //记录账变
                                saveAccountChange(errorOrder, money);
                            }
                        }
                    } else if (txStatus == 0) {
                        //转入AE,本地先扣减，确认三方加点失败后加回本地余额
                        if (orderType == AccountChangeEnum.AE_IN.getType()) {
                            Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + money.stripTrailingZeros().toPlainString() + ",转入AE时加点失败,加回本地额度");
                            if (count > 0) {
                                //加回额度
                                addMoney(errorOrder.getUserId(), money);
                                //记录账变
                                saveAccountChange(errorOrder, money);
                            }
                            //转出AE时，三方先扣减，确认三方扣点失败无需加回本地余额
                        } else if (orderType == AccountChangeEnum.AE_OUT.getType()) {
                            updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出AE时扣点失败,额度未丢失");
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("查询AE交易记录时异常,msg={}", e.getMessage());
            }
        }
    }

    public void addMoney(Long userId, BigDecimal money) {
        userMoneyService.addMoney(userId, money);
        log.info("订单自动补单成功,userMoney表金额补偿成功,userId={},money={}", userId, money);
    }

    public Integer updateErrorOrderStatus(ErrorOrder errorOrder, String remark) {
        //调save方法更新无效，所以自定义方法
        Integer count = errorOrderRepository.updateErrorStatusRemark(1, remark, errorOrder.getId());
        log.info("订单自动补单成功,errorOrder表更新成功,errorOrder={}", errorOrder.toString());
        return count;
    }

    public void saveAccountChange(ErrorOrder errorOrder, BigDecimal money) {
        //账变中心记录账变
        UserMoney userMoney = userMoneyService.findByUserId(errorOrder.getUserId());
        AccountChangeVo accountChangeVo = new AccountChangeVo();
        accountChangeVo.setUserId(errorOrder.getUserId());
        accountChangeVo.setChangeEnum(AccountChangeEnum.SYSTEM_UPP);
        accountChangeVo.setAmount(money);
        accountChangeVo.setAmountAfter(userMoney.getMoney());
        accountChangeVo.setAmountBefore(userMoney.getMoney().subtract(money));
        accountChangeVo.setOrderNo(errorOrder.getOrderNo());
        asyncService.executeAsync(accountChangeVo);
        log.info("订单自动补单成功,AccountChange表账变记录成功,AccountChange={}", accountChangeVo.toString());
    }

    public void tryVNCSupplement(ErrorOrder errorOrder, String vncAccount) {
        String orderNo = errorOrder.getOrderNo();
        int requestNum = 0;
        while (true) {
            try {
                if (requestNum >= 3) {
                    log.error("VNC尝试3次补单失败,errorOrder={}", errorOrder.toString());
                    break;
                }
                requestNum++;
                Thread.sleep(30 * 1000);
                PublicLotteryApi.ResponseEntity checkOrder = lotteryApi.getCheckOrder(orderNo, vncAccount);
                if (checkOrder == null) {
                    log.error("查询VNC交易记录时远程请求异常");
                    continue;
                }
                String errorCode = checkOrder.getErrorCode();
                BigDecimal money = errorOrder.getMoney();
                Integer orderType = errorOrder.getType();
                if ("16".equals(errorCode)) { //转账订单不存在
                    log.info("AE订单号:{}查询无记录", orderNo);
                    //转入AE时，AE查询无记录说明AE加点失败，要把本地的钱加回来，
                    if (orderType == AccountChangeEnum.VNC_IN.getType()) {
                        //更新错误订单表状态
                        Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + money.stripTrailingZeros().toPlainString() + ",转入VNC时加点失败,加回本地额度");
                        if (count > 0) {
                            //加回额度
                            addMoney(errorOrder.getUserId(), money);
                            //记录账变
                            saveAccountChange(errorOrder, money);
                        }
                    } else if (orderType == AccountChangeEnum.VNC_OUT.getType()) {
                        //转出VNC时，是先扣减VNC的钱再加回本地，VNC查询无记录说明没有扣点成功，本地也不用把钱加回来,更新状态就行
                        updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出VNC时扣点失败,额度未丢失");
                    }
                    break;
                }
                if (!"0".equals(errorCode)) {
                    log.info("VNC订单号:{}查询交易记录异常,result={}", orderNo, checkOrder);
                    continue;
                }
                if ("0".equals(errorCode)) {
                    String result = checkOrder.getData();
                    List<TransferVo> records = JSON.parseArray(result, TransferVo.class);
                    for (TransferVo vo : records) {
                        if (vncAccount.equals(vo.getUserName()) && orderNo.equals(vo.getOrderNo())) {
                            //转入wm,本地先扣减，确认三方加点成功无需加回本地余额
                            if (vo.getOrderType() == 8 && errorOrder.getType() == AccountChangeEnum.VNC_IN.getType()) {
                                updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转入VNC时加点成功,额度未丢失");
                                //转出WM时，三方先扣减，确认三方扣点成功加回本地余额
                            } else if (vo.getOrderType() == 9 && errorOrder.getType() == AccountChangeEnum.VNC_OUT.getType()) {
                                //以WM额度为准
                                BigDecimal balance = vo.getMoney();
                                //更新错误订单表状态
                                Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + balance.stripTrailingZeros().toPlainString() + ",转出WM时扣点成功,加回本地额度");
                                if (count > 0) {
                                    //加回额度
                                    addMoney(errorOrder.getUserId(), balance);
                                    //记录账变
                                    saveAccountChange(errorOrder, balance);
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("查询AE交易记录时异常,msg={}", e.getMessage());
            }
        }
    }

    /**
     * 尝试3次补单
     *
     * @param errorOrder
     */
    public void tryDMCSupplement(ErrorOrder errorOrder, Long userId) {
        String orderNo = errorOrder.getOrderNo();
        int requestNum = 0;
        while (true) {
            try {
                if (requestNum >= 3) {
                    log.error("大马彩尝试3次补单失败,errorOrder={}", errorOrder.toString());
                    break;
                }
                requestNum++;
                //报表查询需间隔30秒，未搜寻到数据需间隔10秒。
//                Thread.sleep(30 * 1000);
                //查询转账记录
                JSONObject jsonObject = lotteryDmcApi.getTransactionDetail2(errorOrder.getOrderNo());
                if (jsonObject == null) {
                    log.error("查询DMC交易记录时远程请求异常");
                    continue;
                }
                log.error("查询DMC交易记录时远程请求异常");
                log.info("订单号:{}查询无记录", orderNo);
                //转入DMC时，DMC查询无记录说明DMC加点失败，要把本地的钱加回来，
                boolean flag = checkDMCOrder(jsonObject);
                if (errorOrder.getType() == AccountChangeEnum.DMC_IN.getType()) {
                    //更新错误订单表状态
                    //转入wm,本地先扣减，确认三方加点成功无需加回本地余额
                    if(!flag){
                        Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + errorOrder.getMoney().stripTrailingZeros().toPlainString() + ",转入DMC时加点失败,加回本地额度");
                        if (count > 0) {
                            //加回额度
                            addMoney(errorOrder.getUserId(), errorOrder.getMoney());
                            //记录账变
                            saveAccountChange(errorOrder, errorOrder.getMoney());
                        }
                    }

                } else if (errorOrder.getType() == AccountChangeEnum.DMC_OUT.getType()) {
                    if(flag){
                        //转出DMC时，是先扣减DMC的钱再加回本地，DMC查询无记录说明没有扣点成功，本地也不用把钱加回来,更新状态就行
                        updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出DMC时扣点失败,额度未丢失");
                    }
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("查询DMC交易记录时异常,msg={}", e.getMessage());
            }
        }
    }

    private boolean checkDMCOrder(JSONObject jsonObject) {
        if(jsonObject.containsKey("responseDto")){
            JSONObject responseDto = jsonObject.getJSONObject("responseDto");
            if(responseDto.containsKey("body")){
                JSONArray body = responseDto.getJSONArray("body");
                if(body != null && body.size() > 0){
                    return true;
                }
            }
        }

        return false;
    }

    public void tryDgSupplement(ErrorOrder errorOrder, String dgAccount) {
        String orderNo = errorOrder.getOrderNo();
        int requestNum = 0;
        while (true) {
            try {
                if (requestNum >= 3) {
                    log.error("Dg尝试3次补单失败,errorOrder={}", errorOrder.toString());
                    break;
                }
                requestNum++;
                Thread.sleep(30 * 1000);
                JSONObject apiResponseData = dgApi.checkTransfer(orderNo);
                if (apiResponseData == null) {
                    log.error("查询DG交易记录时远程请求异常");
                    continue;
                }
                String errorCode = apiResponseData.getString("codeId");
                BigDecimal money = errorOrder.getMoney();
                Integer orderType = errorOrder.getType();
                if ("324".equals(errorCode)) { //转账订单不存在
                    log.info("DG订单号:{}查询无记录", orderNo);
                    //转入AE时，AE查询无记录说明AE加点失败，要把本地的钱加回来，
                    if (orderType == AccountChangeEnum.DG_IN.getType()) {
                        //更新错误订单表状态
                        Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + money.stripTrailingZeros().toPlainString() + ",转入DG时加点失败,加回本地额度");
                        if (count > 0) {
                            //加回额度
                            addMoney(errorOrder.getUserId(), money);
                            //记录账变
                            saveAccountChange(errorOrder, money);
                        }
                    } else if (orderType == AccountChangeEnum.DG_OUT.getType()) {
                        //转出VNC时，是先扣减DG的钱再加回本地，DG查询无记录说明没有扣点成功，本地也不用把钱加回来,更新状态就行
                        updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出DG时扣点失败,额度未丢失");
                    }
                    break;
                }
                if (!"0".equals(errorCode)) {
                    log.info("DG订单号:{}查询交易记录异常,result={}", orderNo, apiResponseData);
                    continue;
                }
                if ("0".equals(errorCode)) {
                    //转入DG时，DG查询无记录说明DG加点失败，要把本地的钱加回来，
                    if (errorOrder.getType() == AccountChangeEnum.DG_IN.getType()) {
                        //更新错误订单表状态
                        Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + errorOrder.getMoney().stripTrailingZeros().toPlainString() + ",转入DG时加点失败,加回本地额度");
                        if (count > 0) {
                            //加回额度
                            addMoney(errorOrder.getUserId(), errorOrder.getMoney());
                            //记录账变
                            saveAccountChange(errorOrder, errorOrder.getMoney());
                        }
                    } else if (errorOrder.getType() == AccountChangeEnum.DG_OUT.getType()) {
                        //转出DG时，是先扣减DG的钱再加回本地，DG查询无记录说明没有扣点成功，本地也不用把钱加回来,更新状态就行
                        updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出DG时扣点失败,额度未丢失");
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("查询DG交易记录时异常,msg={}", e.getMessage());
            }
        }
    }

    /**
     * 尝试3次补单
     *
     * @param errorOrder
     * @param thirdAccount
     */
//    public void tryDMCSupplement(ErrorOrder errorOrder, String thirdAccount) {
//        String orderNo = errorOrder.getOrderNo();
//        int requestNum = 0;
//        while (true) {
//            try {
//                if (requestNum >= 3) {
//                    log.error("大马彩尝试3次补单失败,errorOrder={}", errorOrder.toString());
//                    break;
//                }
//                requestNum++;
//                //报表查询需间隔30秒，未搜寻到数据需间隔10秒。
//                Thread.sleep(30 * 1000);
//                //查询转账记录
//                PublicWMApi.ResponseEntity entity = publicLottoApi.getMemberTradeReport(thirdAccount, null, orderNo, null, null, null);
//                if (entity == null) {
//                    log.error("查询WM交易记录时远程请求异常");
//                    continue;
//                }
//                if (entity.getErrorCode() == 107) {
//                    log.info("订单号:{}查询无记录", orderNo);
//                    //转入wm时，wm查询无记录说明wm加点失败，要把本地的钱加回来，
//                    if (errorOrder.getType() == AccountChangeEnum.WM_IN.getType()) {
//                        //更新错误订单表状态
//                        Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + errorOrder.getMoney().stripTrailingZeros().toPlainString() + ",转入WM时加点失败,加回本地额度");
//                        if (count > 0) {
//                            //加回额度
//                            addMoney(errorOrder.getUserId(), errorOrder.getMoney());
//                            //记录账变
//                            saveAccountChange(errorOrder, errorOrder.getMoney());
//                        }
//                    } else if (errorOrder.getType() == AccountChangeEnum.RECOVERY.getType()) {
//                        //转出wm时，是先扣减wm的钱再加回本地，wm查询无记录说明没有扣点成功，本地也不用把钱加回来,更新状态就行
//                        updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转出WM时扣点失败,额度未丢失");
//                    }
//                    break;
//                }
//                if (entity.getErrorCode() != 0) {
//                    log.info("订单号:{}查询交易记录异常,result={}", orderNo, entity.toString());
//                    continue;
//                }
//                if (entity.getErrorCode() == 0) {
//                    String result = entity.getResult().toString();
//                    List<WmMemberTradeReportVo> records = JSON.parseArray(result, WmMemberTradeReportVo.class);
//                    for (WmMemberTradeReportVo vo : records) {
//                        if (thirdAccount.equals(vo.getUser()) && orderNo.equals(vo.getOrdernum())) {
//                            //转入wm,本地先扣减，确认三方加点成功无需加回本地余额
//                            if (vo.getOp_code() == 121 && errorOrder.getType() == AccountChangeEnum.WM_IN.getType()) {
//                                updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:0,转入WM时加点成功,额度未丢失");
//                                //转出WM时，三方先扣减，确认三方扣点成功加回本地余额
//                            } else if (vo.getOp_code() == 122 && errorOrder.getType() == AccountChangeEnum.RECOVERY.getType()) {
//                                //以WM额度为准
//                                BigDecimal money = new BigDecimal(vo.getMoney()).abs();
//                                //更新错误订单表状态
//                                Integer count = updateErrorOrderStatus(errorOrder, "自动补单成功，补单金额:" + money.stripTrailingZeros().toPlainString() + ",转出WM时扣点成功,加回本地额度");
//                                if (count > 0) {
//                                    //加回额度
//                                    addMoney(errorOrder.getUserId(), money);
//                                    //记录账变
//                                    saveAccountChange(errorOrder, money);
//                                }
//                            }
//                            break;
//                        }
//                    }
//                    break;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("查询WM交易记录时异常,msg={}", e.getMessage());
//            }
//        }
//    }
}
