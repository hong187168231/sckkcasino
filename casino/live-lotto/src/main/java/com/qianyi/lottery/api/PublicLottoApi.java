package com.qianyi.lottery.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.lottery.constants.LottoConfig;
import com.qianyi.lottery.util.EncryptUtil;
import com.qianyi.lottery.util.HttpClient4Util;
import com.qianyi.lottery.util.StringUtils;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;


@Component
@Slf4j
public class PublicLottoApi {

    @Autowired
    private LottoConfig lottoConfig;

    public static final String SUCCESS_CODE = "0000";


    /**
     * 获取令牌
     *
     * @return
     */
    public String fetchToken() {
        Map<String, Object> params = new HashMap<>();
        params.put("merchant_id", 1);
        params.put("secret_key", "aU7RC1JC6qTFv73Rs8kkmSekhmCUXw00O1JloFU");
        String json = JSON.toJSONString(params);
        log.info("Lotto fetchToken参数{}", params);
        String url = lottoConfig.getApiUrl() + "/frontend-api/fetchToken";
        String result = HttpClient4Util.doPostJson(url, json, null);
        log.info("Lotto fetchToken结果{}", result);
        JSONObject jsonObject = analysisResult(result);
        return getGameToken(jsonObject);
    }


    /**
     * 登录游戏
     *
     * @param userName
     * @param customerId
     * @return
     */
    public String loginDmcGame(String userName, String customerId) {
        String url = "http://api.kk-lotto.com:8080/api/userLottoLogin";
        Map<String, Object> params = new HashMap<>();
        params.put("enterprise_id", 3);
        params.put("user_name", userName);
        params.put("customer_id", customerId);
        params.put("language", "ch");
        String json = JSON.toJSONString(params);
        log.info("Lotto fetchToken参数{}", params);
        String result = HttpClient4Util.doPostJson(url, json, "d41d8cd98f00b204e9800998ecf8427e");
        log.info("大马彩登录游戏结果1：{} 结果二", lottoConfig.getEnterpriseId(), result);
        log.info("大马彩登录游戏结果：{}", result);
        log.info("Lotto fetchToken结果{}", result);
        result = result.substring(result.indexOf("<form"));
        return result;
    }

    /**
     * 加扣点
     *
     * @param customer_id
     * @param customer_name
     * @param transterType  1: 加点， 2：扣点
     * @return
     */
    public JSONObject transterWallet(String customer_id, String customer_name, BigDecimal amount, Integer transterType, String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("customer_id", customer_id);
        params.put("amount", amount);
        params.put("mode", transterType == 1 ? "Credit" : "Debit");
        params.put("merchant_id", 1);

        String json = JSON.toJSONString(params);
        log.info("Lotto transterWallet{}", params);
        String url = lottoConfig.getApiUrl() + "/frontend-api/wallet/updateWallet";
        String result = HttpClient4Util.doPostJson(url, json, token);
        log.info("Lotto transterWallet{}", result);
        return analysisResult(result);
    }


    /**
     * 查余额
     *
     * @param customer_id_list
     * @return
     */
    public BigDecimal fetchWalletBalance(List<String> customer_id_list, String token) {
        Map<String, Object> params = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        params.put("merchant_id", 1);
        if (CollUtil.isNotEmpty(customer_id_list)) {
            for (String customer_id : customer_id_list) {
                Map<String, Object> customer = new HashMap<>();
                customer.put("customer_id", customer_id);
                list.add(customer);
            }
            params.put("customer_id_list", list);
        }
        String json = JSON.toJSONString(params);
        log.info("Lotto获取fetchWalletBalance参数{}", params);
        String url = lottoConfig.getApiUrl() + "/frontend-api/wallet/fetchWalletBalance";
        String result = HttpClient4Util.doPostJson(url, json, token);
        log.info("Lotto获取fetchWalletBalance结果{}", result);
        JSONObject jsonObject = analysisResult(result);
        //得到总余额
        return getGameBalance(jsonObject);
    }


    /**
     * 查余额
     *
     * @return
     */
    public BigDecimal fetchTotalBalance( String token) {
        Map<String, Object> params = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        params.put("merchant_id", 1);
        String json = JSON.toJSONString(params);
        log.info("Lotto获取fetchWalletBalance参数{}", params);
        String url = lottoConfig.getApiUrl() + "/frontend-api/wallet/fetchWalletBalance";
        String result = HttpClient4Util.doPostJson(url, json, token);
        log.info("Lotto获取fetchWalletBalance结果{}", result);
        JSONObject jsonObject = analysisResult(result);
        //得到总余额
        return getGameTotalBalance(jsonObject);
    }


    /**
     * 大马彩是UTC-7时间查询的数据
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
        Map<String, Object> params = getCommonParams();
        params.put("merchant_id", 1);
        params.put("customer", null);
        params.put("reference_number", null);
        params.put("sdate", "2022-07-27 19:41:13");
        params.put("edate", "2022-09-27 19:41:13");
        params.put("flag", null);

        String url = lottoConfig.getApiUrl() + "/api/fetchBettingReport";
        String json = JSON.toJSONString(params);
        String result = HttpClient4Util.doPostJson(url, json, "d41d8cd98f00b204e9800998ecf8427e");
        log.info("Lotto fetchToken结果{}", result);
        JSONObject jsonObject = analysisResult(result);
        return result;
    }


    public JSONObject getDateTimeReport2(String account, String startTime, String endTime) throws Exception {
//        Date date = DateUtils.addHours(DateUtil.getDatePatten(startTime), -1);
//        Date endDate = DateUtils.addHours(DateUtil.getDatePatten(endTime), -1);
//        startTime = DateUtil.dateToPatten(date);
//        endTime = DateUtil.dateToPatten(endDate);
        Map<String, Object> params = new HashMap<>();
        params.put("merchant_id", 1);
        params.put("customer", null);
        params.put("reference_number", null);
        params.put("sdate", "2022-07-27 19:41:13");
        params.put("edate", "2022-10-27 19:41:13");
        params.put("flag", null);
        String token = fetchToken();
        String url = lottoConfig.getApiUrl() + "/api/fetchBettingReport";
        String json = JSON.toJSONString(params);
        String result = HttpClient4Util.doPostJson(url, json, token);
        log.info("Lotto fetchToken结果{}", result);
        JSONObject jsonObject = analysisResult(result);
        return jsonObject;
    }

    private Map<String, Object> getCommonParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("enterprise_id", lottoConfig.getEnterpriseId());
        params.put("currency", lottoConfig.getCurrency());
        return params;
    }

    private JSONObject analysisResult(String result) {
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            return jsonObject;
        } catch (Exception e) {
            log.error("解析大马彩据时出错，result={},msg={}", result, e.getMessage());
        }
        return null;
    }

    public String getGameToken(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        //得到状态码
        boolean code = getResultCode(jsonObject);
        if (code && jsonObject.containsKey("token")) {
            return jsonObject.getString("token");
        }
        return null;
    }

    public BigDecimal getGameBalance(JSONObject jsonObject) {
        BigDecimal balance = BigDecimal.ZERO;
        if (jsonObject == null) {
            return null;
        }
        //得到状态码
        boolean code = getResultCode(jsonObject);
        if (code && jsonObject.containsKey("data")) {
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray customerList = data.getJSONArray("customer_id_list");
            if (CollUtil.isNotEmpty(customerList)) {
                for (Object jsonOb : customerList) {
                    try {
                        JSONObject parseObject = JSONObject.parseObject(jsonOb.toString());
                        balance = balance.add(parseObject.getBigDecimal("account_balance"));
                    } catch (Exception ex) {
                        log.info("余额添加失败：【{}】", jsonOb);
                    }
                }
            }

        }
        return balance;
    }


    public BigDecimal getGameTotalBalance(JSONObject jsonObject) {
        BigDecimal balance = BigDecimal.ZERO;
        if (jsonObject == null) {
            return null;
        }
        //得到状态码
        boolean code = getResultCode(jsonObject);
        if (code && jsonObject.containsKey("data")) {
            JSONObject data = jsonObject.getJSONObject("data");
            try {
                String totalBalance = data.getString("total_balance");
                balance = balance.add(new BigDecimal(totalBalance));
            } catch (Exception ex) {
                log.info("获取总余额失败：【{}】", data);
            }
        }
        return balance;
    }

    public static PublicLotteryApi.ResponseEntity entity(String result) {
        if (StringUtils.isBlank(result)) {
            return null;
        }
        PublicLotteryApi.ResponseEntity entity = new PublicLotteryApi.ResponseEntity();
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            entity.setErrorCode("500");
            entity.setErrorMessage("远程请求大马彩异常,请重新操作");
            return entity;
        }
        if (ObjectUtil.isEmpty(jsonObject)) {
            return null;
        }
        if (ObjectUtil.isNotEmpty(jsonObject)) {
            Boolean success = jsonObject.getBoolean("success");
            if (success) {
                entity.setData(jsonObject.getString("data"));
                entity.setErrorCode("0");
            }
        }
        return entity;
    }


    public boolean getResultCode(JSONObject jsonObject) {
        String successObj = jsonObject.getString("success");
        if (StringUtils.isNotBlank(successObj)) {
            return true;
        }
        return false;
    }

    public String getTransterId(JSONObject jsonObject) {
        boolean code = getResultCode(jsonObject);
        if (code && jsonObject.containsKey("token")) {
            return jsonObject.getString("token");
        }
        return "";
    }


}
