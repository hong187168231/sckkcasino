package com.qianyi.lottery.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.lottery.constants.LottoConfig;
import com.qianyi.lottery.util.EncryptUtil;
import com.qianyi.lottery.util.HttpClient4Util;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PublicLotteryApi {

    @Autowired
    private LottoConfig lottoConfig;

    //url地址
    @Value("${project.ync.apiUrl:null}")
    private String apiUrl;

    // 平台ID
    @Value("${project.ync.platformId:null}")
    private String platformId;

    // 商户号
    @Value("${project.ync.merchantCode:null}")
    private String merchantCode;

    @Value("${project.ync.aesKey:null}")
    private String aesKey;

    @Value("${project.ync.md5Key:null}")
    private String md5Key;

    public static final String SUCCESS_CODE = "0000";


    /**
     *
     * @param playerName
     * @param currency
     * @return
     */
    public boolean createMember(String playerName, String currency) {
        String url = apiUrl + "/createMember";
        Map<String, Object> map = new HashMap<>();
        long longTime = System.currentTimeMillis();
        String passWord = playerName + merchantCode;
        map.put("currentTime", longTime);
        map.put("merchantCode", merchantCode);
        map.put("platformId", platformId);
        map.put("playerName", playerName);
        map.put("passWord", passWord);
        StringBuilder sb = new StringBuilder();
        String token = EncryptUtil.md5(longTime + merchantCode + platformId + playerName + passWord + md5Key);
        map.put("token", token);
        map.put("currency", currency);
        String aesJson = JSON.toJSONString(map);
        String params = EncryptUtil.aesEncrypt(aesKey, aesJson);
        log.info("越南彩创建会员请求参数明文：{}：", JSONObject.toJSONString(map));
        String result = sendPostRequest(url, merchantCode, params);
        log.info("越南彩创建会员结果：{}：", result);
        ResponseEntity entity = entity(result);
        JSONObject jsonData = null;
        if (entity != null && ObjectUtils.isEmpty(entity.getErrorCode())) {
            jsonData = JSONObject.parseObject(entity.getData());
        }

        if ("0".equals(entity.getErrorCode())) {
            return true;
        }
        return false;

    }

    private static ResponseEntity entity(String result) {
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
        JSONObject error = jsonObject.getJSONObject("resp_msg");
        if (error != null) {
            String code = error.getString("resp_code");
            String message = error.getString("resp_msg");
            entity.setErrorCode(code);
            entity.setErrorMessage(message);
        }
        String data = jsonObject.getString("datas");
        entity.setData(data);
        return entity;
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

    /**
     * 登录
     *
     * @param customer_id
     * @param customer_name
     * @param customer_mail
     * @return
     */
    public JSONObject userLottoLogin(String customer_id, String customer_name, String customer_mail) {
        return null;
    }

    /**
     * 加点
     * @param customer_id
     * @param customer_name
     * @param amount_to_add
     * @param customer_mail
     * @return
     */
    public JSONObject addInWallet(String customer_id, String customer_name, String amount_to_add, String customer_mail) {
        return null;
    }


    /**
     * 转出
     * @param customer_id
     * @param customer_name
     * @param customer_mail
     * @param amount
     * @return
     */
    public JSONObject updateWallet(String customer_id, String customer_name,  String customer_mail,String amount) {
        return null;
    }


    /**
     * 查余额
     * @param customer_id_list
     * @return
     */
    public JSONObject fetchWalletBalance(List<String> customer_id_list) {
        return null;
    }



}
