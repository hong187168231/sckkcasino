package com.qianyi.lottery.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.lottery.util.EncryptUtil;
import com.qianyi.lottery.util.HttpClient4Util;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PublicLotteryApi {

    //url地址
    @Value("${project.vnc.apiUrl:null}")
    private String apiUrl;

    // 平台ID
    @Value("${project.vnc.platformId:null}")
    private String platformId;

    // 商户号
    @Value("${project.vnc.merchantCode:null}")
    private String merchantCode;

    @Value("${project.vnc.aesKey:null}")
    private String aesKey;

    @Value("${project.vnc.md5Key:null}")
    private String md5Key;

    @Value("${project.vnc.currency:null}")
    private String currency;

    public static final String SUCCESS_CODE = "0000";


    /**
     *
     * 创建会员
     * @param playerName
     * @return
     */
    public boolean createMember(String playerName) {
        String url = apiUrl + "/createMember";
        Map<String, Object> map = new HashMap<>();
        setBasicParams(playerName, map);
        String passWord = playerName + merchantCode;
        map.put("passWord", passWord);
        String token = EncryptUtil.md5(map.get("currentTime") + merchantCode + platformId + playerName + passWord + md5Key);
        map.put("token", token);
        map.put("currency", currency);
        String aesJson = JSON.toJSONString(map);
        log.info("越南彩创建会员请求参数明文：{}", JSONObject.toJSONString(map));
        String result = sendPostRequest(url, merchantCode, aesJson);
        log.info("越南彩创建会员结果：{}", result);
        ResponseEntity entity = entity(result);
        if (entity == null || ObjectUtils.isEmpty(entity.getErrorCode())) {
            return false;
        }

        if ("0".equals(entity.getErrorCode())) {
            return true;
        }
        return false;

    }


    /**
     * 登录游戏
     *
     * @param playerName
     * @param language
     * @param isMobile
     * @param gameCode
     * @return
     */
    public String lanuchGame(String playerName, String language,
                             String isMobile, String gameCode) {

        String url = apiUrl + "/launchGame";
        Map<String, Object> map = new HashMap<>();
        setBasicParams(playerName, map);
        String passWord = playerName + merchantCode;
        map.put("passWord", passWord);
        map.put("gameCode", gameCode);
        map.put("language", language);
        map.put("isMobile", isMobile);
        String token = EncryptUtil.md5(map.get("currentTime") + merchantCode + platformId +
                playerName + gameCode+ passWord + isMobile + language + md5Key);
        map.put("token", token);
        String aesJson = JSON.toJSONString(map);
        log.info("越南彩登录游戏请求参数明文：{}", JSONObject.toJSONString(map));
        String result = sendPostRequest(url, merchantCode, aesJson);
        log.info("越南彩登录游戏结果：{}", result);
        ResponseEntity entity = entity(result);
        if (entity == null || ObjectUtils.isEmpty(entity.getErrorCode())) {
            return null;
        }

        if ("0".equals(entity.getErrorCode())) {
            return entity.getData();
        }
        return null;
    }


    /**
     * 上下分
     *
     * @param playerName
     * @param transferType
     * @param amount
     * @param orderNo
     * @return
     */
    public ResponseEntity changeBalance(String playerName, Integer transferType, BigDecimal amount, String orderNo) {
        String amountStr = amount.stripTrailingZeros().toPlainString();
        String url = apiUrl + "/walletTransfer";
        Map<String, Object> map = new HashMap<>();
        setBasicParams(playerName, map);
        map.put("transferType", transferType);
        map.put("amount", amountStr);
        map.put("orderNo", orderNo);
        String token = EncryptUtil.md5(map.get("currentTime") + merchantCode + platformId +
                playerName + transferType+ amountStr + orderNo + md5Key);
        map.put("token", token);
        String aesJson = JSON.toJSONString(map);

        log.info("越南彩转账请求参数明文：{}", JSONObject.toJSONString(map));
        String result = sendPostRequest(url, merchantCode, aesJson);
        log.info("越南彩转账请求结果：{}", result);
        ResponseEntity entity = entity(result);
        if (entity == null || ObjectUtils.isEmpty(entity.getErrorCode())) {
            return null;
        }

        return entity;
    }

    /**
     * 查询余额
     *
     * @param playerName
     * @return
     */
    public BigDecimal getBalance(String playerName) {
        String url = apiUrl + "/queryBalance";
        Map<String, Object> map = new HashMap<>();
        setBasicParams(playerName, map);
        String token = EncryptUtil.md5(map.get("currentTime") + merchantCode + platformId +
                playerName + md5Key);

        map.put("token", token);
        String aesJson = JSON.toJSONString(map);
        log.info("越南彩查询余额请求参数明文：{}", JSONObject.toJSONString(map));
        String result = sendPostRequest(url, merchantCode, aesJson);
        log.info("越南彩查询余额请求结果：{}", result);
        ResponseEntity entity = entity(result);
        if (entity == null || ObjectUtils.isEmpty(entity.getErrorCode()) || ObjectUtils.isEmpty(entity.getData())) {
            log.error("playerName:{},查询越南彩余额失败,远程请求异常， 返回数据：【{}】", playerName, result);
            return BigDecimal.ZERO;
        }
        return new BigDecimal(entity.getData());
    }


    /**
     * 确认转账订单状态
     *
     * @param orderNo
     * @param playerName
     * @return
     */
    public ResponseEntity getCheckOrder(String orderNo, String playerName){
        String url = apiUrl + "/checkOrder";
        Map<String, Object> map = new HashMap<>();
        setBasicParams(playerName, map);
        String token = EncryptUtil.md5(map.get("currentTime") + merchantCode + platformId +
                playerName + orderNo + md5Key);
        map.put("token", token);
        map.put("orderNo", orderNo);
        String aesJson = JSON.toJSONString(map);

        log.info("越南彩确认订单状态请求参数明文：{}", JSONObject.toJSONString(map));
        String result = sendPostRequest(url, merchantCode, aesJson);
        log.info("越南彩确认订单状态请求结果：{}", result);
        ResponseEntity entity = entity(result);
        if (entity == null || ObjectUtils.isEmpty(entity.getErrorCode())) {
            return null;
        }
        return entity;
    }



    /**
     * 越南彩是UTC-7时间查询的数据
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public String getDateTimeReport(String startTime, String endTime, String playerName) throws Exception {
        Date date = DateUtils.addHours(DateUtil.getDatePatten(startTime), -1);
        Date endDate = DateUtils.addHours(DateUtil.getDatePatten(endTime), -1);

        startTime = DateUtil.dateToPatten(date);
        endTime = DateUtil.dateToPatten(endDate);

//        startTime = "2022-09-27 17:40:00";
//        endTime = "2022-09-27 17:50:00";
        String url = apiUrl + "/gameBetInfo";
        Map<String, Object> map = new HashMap<>();
        setBasicParams(playerName, map);
        String token = EncryptUtil.md5(map.get("currentTime") + merchantCode + platformId +
                playerName + md5Key);

        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("playerName", playerName);
        map.put("token", token);
        String aesJson = JSON.toJSONString(map);
        log.info("越南彩查询下注记录请求参数明文：{}", JSONObject.toJSONString(map));
        String result = sendPostRequest(url, merchantCode, aesJson);
        log.info("越南彩查询下注记录请求结果：{}", result);

        if (CommonUtil.checkNull(result)) {
            return null;
        }
        return result;
    }


    /**
     * 基础属性
     *
     * @param playerName
     * @param map
     */
    private void setBasicParams(String playerName, Map<String, Object> map) {
        long longTime = System.currentTimeMillis();
        map.put("currentTime", longTime);
        map.put("platformId", platformId);
        map.put("playerName", playerName);
        map.put("merchantCode", merchantCode);
    }

    public static ResponseEntity entity(String result) {
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }
        ResponseEntity entity = new ResponseEntity();
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            entity.setErrorCode("500");
            entity.setErrorMessage("远程请求越南彩异常,请重新操作");
            return entity;
        }
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        if (jsonObject != null) {
            String code = jsonObject.getString("resp_code");
            String message = jsonObject.getString("resp_msg");
            entity.setErrorCode(code);
            entity.setErrorMessage(message);
        }
        String data = jsonObject.getString("datas");
        entity.setData(data);
        return entity;
    }

    public String getDateTimeDetailReport(String startTime, String endTime, String playerName)  throws Exception{
        Date date = DateUtils.addHours(DateUtil.getDatePatten(startTime), -1);
        Date endDate = DateUtils.addHours(DateUtil.getDatePatten(endTime), -1);

        startTime = DateUtil.dateToPatten(date);
        endTime = DateUtil.dateToPatten(endDate);

//        startTime = "2022-12-16 18:35:30";
//        endTime = "2022-12-16 18:40:30";

        log.info("越南彩拉单时间开始时间：【{}】，结束时间：【{}】", startTime, endTime);
        String url = apiUrl + "/gameBetDetailInfo";
        Map<String, Object> map = new HashMap<>();
        setBasicParams(playerName, map);
        String token = EncryptUtil.md5(map.get("currentTime") + merchantCode + platformId +
                playerName + md5Key);

        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("playerName", playerName);
        map.put("token", token);
        String aesJson = JSON.toJSONString(map);
        log.info("越南彩查询下注详情记录请求参数明文：{}", JSONObject.toJSONString(map));
        String result = sendPostRequest(url, merchantCode, aesJson);
        log.info("越南彩查询下注详情记录请求结果：{}", result);

        if (CommonUtil.checkNull(result)) {
            return null;
        }
        return result;
    }


    @Data
    public static class ResponseEntity {

        private String errorCode;

        private String errorMessage;

        private String data;
    }

    /**
     * 发送请求去三方获取数据
     *
     * @param url
     * @param merchantCode
     * @param params
     * @return
     */
    private String sendPostRequest(String url, String merchantCode, String params) {
        Map<String, Object> map = new HashMap<>();
        map.put("merchantCode", merchantCode);
        map.put("params", params);

        return HttpClient4Util.doPost(url, map);
    }

}
