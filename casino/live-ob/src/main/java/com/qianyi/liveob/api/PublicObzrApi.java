package com.qianyi.liveob.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.liveob.dto.*;
import com.qianyi.liveob.utils.*;
import com.qianyi.liveob.utils.encrypt.AESUtil;
import com.qianyi.liveob.utils.encrypt.Md5Util;
import com.qianyi.liveob.utils.http.HttpClientHandler;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 接口文档  http://api-doc.sportxxxw1box.com/#/main/api_doc_zn
 */
@Component
@Slf4j
public class PublicObzrApi {

    @Value("${project.obzr.merchantCode:null}")
    private String merchantCode;

    @Value("${project.obzr.currency:null}")
    private String currency;
    @Value("${project.obzr.apiUrl:null}")
    private String apiUrl;
    @Value("${project.obzr.apiDataUrl:null}")
    private String apiDataUrl;
    @Value("${project.obzr.aesKey:null}")
    private String aesKey;

    @Value("${project.obzr.md5Key:null}")
    private String md5Key;

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public static final String SUCCESS_CODE = "200";


    public ResponseEntity forwardGame(String userName, Integer deviceType) {
        ReqLoginGameDTO dto = new ReqLoginGameDTO();
        dto.setLoginName(userName);
        dto.setLoginPassword("kk" + userName);
        dto.setLang(1);
        dto.setDeviceType(deviceType);
        dto.setBackurl("http://baidu.com/");
        dto.setTimestamp(System.currentTimeMillis());
        String result = submit("/forwardGame", dto);
        System.out.println(result);
        PublicObzrApi.ResponseEntity entity = entity(result);
        return entity;

    }


    /**
     * 注册
     *
     * @param userName 用户(可以包含但是不要等同于特殊字符或者空格,长度控制在30个字符以下)
     * @param nickname 昵称，非必填
     * @return
     */
    public boolean create(String userName, String nickname) {
        ReqCreatePlayerDTO dto = new ReqCreatePlayerDTO();
        dto.setLoginName(userName);
        dto.setLoginPassword("kk" + userName);
        dto.setLang(1);
        dto.setTimestamp(System.currentTimeMillis());
        String result = submit("/create", dto);
        log.info("OB体育创建玩家账号结果{}", result);
        ResponseEntity entity = entity(result);
        if (entity == null) {
            log.error("OB电竞创建玩家账号出错,远程请求异常");
        }
        if (SUCCESS_CODE.equals(entity.getCode())) {
            return true;
        }
        log.error("OB进入创建玩家账号出错{}", JSONObject.toJSONString(entity));
        return false;
    }


    /**
     * 踢出用户
     *
     * @param userName
     * @return
     */
    public boolean foreLeaveTable(String userName) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("loginName", userName);
        dto.put("timestamp", System.currentTimeMillis());
        String result = submit("/foreLeaveTable", dto);
        ResponseEntity entity = entity(result);
        if (entity == null) {
            log.error("OB电真人调用离桌接口,远程请求异常");
        }
        if (SUCCESS_CODE.equals(entity.getCode())) {
            return true;
        }
        log.error("OB电真人调用离桌接口出错{}", entity.getData());
        return false;
    }


    private String submit(String method, Object object) {
        String source = GsonUtil.getInstance().toJson(object);
        RequestParamDTO model = new RequestParamDTO();
        model.setMerchantCode(merchantCode);
        model.setParams(AESUtil.encrypt(source, aesKey));
        model.setSignature(Md5Util.getMD5(source + md5Key));
        System.out.println(model);
        String json = GsonUtil.getInstance().toJson(model);
        try {
            if (method.equals("/create") || method.equals("/forwardGame")) {
                return HttpClientHandler.post(apiUrl.concat(method).concat("/v2"), json);
            } else {
                return HttpClientHandler.post(apiUrl.concat(method).concat("/v1"), json);
            }
        } catch (IOException e) {
            log.error("");
            return "";
        }
    }

    private String submitData(String method, Object object, Map<String, String> headers) {
        String source = GsonUtil.getInstance().toJson(object);
        RequestParamDTO model = new RequestParamDTO();
        model.setMerchantCode(merchantCode);
        model.setParams(AESUtil.encrypt(source, aesKey));
        model.setSignature(Md5Util.getMD5(source + md5Key));

        String json = GsonUtil.getInstance().toJson(model);
        try {
            return HttpClientHandler.postJson(apiDataUrl.concat(method).concat("/v1"), json, headers);
        } catch (IOException e) {
            log.error("");
            return "";
        }
    }


    /**
     * 查询用户余额
     *
     * @param userName
     * @return
     */
    public ResponseEntity checkBalance(String userName) {
        ReqGetBalanceDTO dto = new ReqGetBalanceDTO();
        dto.setLoginName(userName);
        dto.setTimestamp(System.currentTimeMillis());
        String result = submit("/balance", dto);
        System.out.println(result);
        ResponseEntity entity = entity(result);
        return entity;
    }


    public ResponseEntity deposit(String userName, BigDecimal amount, String transferId) {
        ReqTransferDTO dto = new ReqTransferDTO();
        dto.setLoginName(userName);
        dto.setAmount(amount);
        dto.setTransferNo(transferId);
        dto.setTimestamp(System.currentTimeMillis());
        String result = submit("/deposit", dto);
        System.out.println(result);
        PublicObzrApi.ResponseEntity entity = entity(result);
        return entity;
    }

    public ResponseEntity withdraw(String userName, BigDecimal amount, String transferId) {
        ReqTransferDTO dto = new ReqTransferDTO();
        dto.setLoginName(userName);
        dto.setAmount(amount);
        dto.setTransferNo(transferId);
        dto.setTimestamp(System.currentTimeMillis());
        String result = submit("/withdraw", dto);
        PublicObzrApi.ResponseEntity entity = entity(result);
        return entity;
    }

    public ResponseEntity transfer(String userName, String transferId) {
        ReqGetTransferDTO dto = new ReqGetTransferDTO();
        dto.setLoginName(userName);
        dto.setTransferNo(transferId);
        dto.setTimestamp(System.currentTimeMillis());
        String result = submit("/transfer", dto);
        PublicObzrApi.ResponseEntity entity = entity(result);
        return entity;
    }


    public ResultDTO betHistoryRecord(String startTime, String endTime, Integer pageIndex) {
        ReqBetRecordHistoryDTO dto = new ReqBetRecordHistoryDTO();
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setPageIndex(pageIndex);
        dto.setTimestamp(System.currentTimeMillis());

        Map<String, String> headers = new HashMap<>();
        headers.put("merchantCode", merchantCode);
        headers.put("pageIndex", "1");

        String result = submitData("/betHistoryRecord", dto, headers);
        ResultDTO entity = resultEntity(result);
        log.info(" OBZR 返回注单结果 ======[{}]", JSON.toJSONString(entity.getData()));
        return entity;
    }


    private static ResponseEntity entity(String result) {
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }
        JSONObject jsonObject = null;
        ResponseEntity entity = new ResponseEntity();
        try {
            jsonObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            String message = "解析OB体育数据时出错,msg=" + e.getMessage();
            log.error(message);
            entity.setCode("500");
            entity.setMessage(message);
            entity.setData("远程请求OB体育异常");
            return entity;
        }
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        entity.setCode(jsonObject.getString("code"));
        entity.setData(jsonObject.getString("data"));
        entity.setMessage(jsonObject.getString("message"));
        return entity;
    }

    private static ResultDTO resultEntity(String result) {
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }
        JSONObject jsonObject = null;
        ResultDTO entity = new ResultDTO();
        try {
            jsonObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            String msg = "解析OB体育数据时出错,msg=" + e.getMessage();
            log.error(msg);
            entity.setCode("500");
            entity.setMessage(msg);
            entity.setData(null);
            return entity;
        }
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        entity.setCode(jsonObject.getString("code"));
        entity.setData(JSONObject.parseObject(jsonObject.getString("data"), PageRespDTO.class));
        entity.setMessage(jsonObject.getString("msg"));
        return entity;
    }



    @Data
    public static class ResponseEntity {

        private String code;

        private String message;

        private String data;
    }
}