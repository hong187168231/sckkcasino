package com.qianyi.livewm.api;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

@Component
public class PublicWMApi {

    private String url = "https://api.a45.me/api/public/Gateway.php";

    @Value("${project.vendorId:0}")
    String vendorId;
    @Value("${project.signature:0}")
    String signature;

    //注册
    public boolean register(String user, String password, String username, String maxwin
            , String maxlose, String mark, String syslang) {
        String cmd = "MemberRegister";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("password", password);
        params.put("username", username);
        if (!ObjectUtils.isEmpty(maxwin)) {
            params.put("maxwin", maxwin);
        }
        if (!ObjectUtils.isEmpty(maxlose)) {
            params.put("maxlose", maxlose);
        }
        if (!ObjectUtils.isEmpty(mark)) {
            params.put("mark", mark);
        }

        params.put("timestamp", timestamp);

        if (!ObjectUtils.isEmpty(syslang)) {
            params.put("syslang", syslang);
        }


        String s = HttpClient4Util.doPost(url, params);

        if (s == null) {
            return false;
        }

        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            return true;
        }
        return false;
    }

    //开游戏
    public String openGame(String user, String password, Integer lang, String voice, Integer ui, String model) {
        String cmd = "SigninGame";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("password", password);
        params.put("lang", lang);
        if (!CommonUtil.checkNull(voice)) {
            params.put("voice", voice);
        }
        if (!CommonUtil.checkNull(model)) {
            params.put("model", model);
        }
        params.put("ui", ui);
        params.put("timestamp", timestamp);

        String s = HttpClient4Util.doPost(url, params);

        if (CommonUtil.checkNull(s)) {
            return null;
        }

        ResponseEntity entity = entity(s);
        return (String) entity.getResult();
    }

    public Boolean logoutGame(String user,Integer syslang){
        String cmd = "LogoutGame";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("timestamp", timestamp);
        if (syslang != null) {
            params.put("syslang", syslang);
        }
        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return null;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            return true;
        }

        return false;

    }

    //加扣点
    public Boolean changeBalance(String user, BigDecimal money, String order, Integer syslang) {
        String cmd = "ChangeBalance";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("money", money.floatValue());
        if(!ObjectUtils.isEmpty(order)){
            params.put("order", order);
        }
        params.put("timestamp", timestamp);
        if (syslang != null) {
            params.put("syslang", syslang);
        }

        String s = HttpClient4Util.doPost(url, params);

        if (CommonUtil.checkNull(s)) {
            return null;
        }

        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            return true;
        }

        return false;

    }

    //查询代理商余额
    public BigDecimal getAgentBalance(Integer syslang) throws Exception {
        String cmd = "GetAgentBalance";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("timestamp", timestamp);
        if (syslang != null) {
            params.put("syslang", syslang);
        }

        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return null;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            float balance = (float) entity.getResult();
            BigDecimal decimal = new BigDecimal(Float.toString(balance));
            return decimal.setScale(2);
        }
        throw new Exception("未获取到信息");
    }

    //取用户余额
    public BigDecimal getBalance(String user, Integer syslang) throws Exception {
        String cmd = "GetBalance";

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("timestamp", getTimestamp());
        if (syslang != null) {
            params.put("syslang", syslang);
        }

        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return null;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
//            float balance = (float) entity.getResult();
//            BigDecimal decimal = new BigDecimal(Float.toString(balance));
            BigDecimal decimal = new BigDecimal(String.valueOf(entity.getResult()));
            return decimal.setScale(2);
        }
        throw new Exception("未获取到信息");
    }

    //开关游戏帐号的登陆和下注
    //type: login:啟用, bet: 下注
    //status: Y:启用, N:停用
    //syslang: 	0:中文, 1:英文(非必要)
    public boolean switchGame(String user, String type, String status, Integer syslang) {
        String cmd = "EnableorDisablemem";


        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("type", type);
        params.put("status", status);
        params.put("timestamp", getTimestamp());
        if (syslang != null) {
            params.put("syslang", syslang);
        }

        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return false;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 报表查询需间隔30秒，未搜寻到数据需间隔10秒。
     * 如欲查询全部用户就不用输入user，会以时间区间进行呈现。
     * 如果报表是要搜寻到起始时间到现在，endTime即可以不用代值，只需带startTime
     * @param user 帐号
     * @param startTime 例:20170809130500 (2017年08月09日 13点05分00秒)
     * @param endTime 	例:20170809130600 (2017年08月09日 13点06分00秒)
     * @param syslang 0:中文, 1:英文(非必要)
     * @param timetype 0:抓下注时间, 1:抓结算时间
     * @param datatype 0:输赢报表, 1:小费报表, 2:全部
     * @param gameno1 期数
     * @param gameno2 局号 (非必要)
     * @return
     * @throws Exception
     */
    public String getDateTimeReport(String user, String startTime, String endTime, Integer syslang, Integer timetype, Integer datatype, Integer gameno1, Integer gameno2) throws Exception {
        String cmd = "GetDateTimeReport";
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        if (!ObjectUtils.isEmpty(user)) {
            params.put("user", user);
        }
        params.put("startTime", startTime);
        if(!ObjectUtils.isEmpty(endTime)){
            params.put("endTime", endTime);
        }
        params.put("timestamp", getTimestamp());
        params.put("timetype", timetype);
        params.put("datatype", datatype);
        if (syslang != null) {
            params.put("syslang", syslang);
        }
        if (gameno1 != null) {
            params.put("gameno1", gameno1);
        }
        if (gameno2 != null) {
            params.put("gameno2", gameno2);
        }
        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return null;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            return String.valueOf(entity.getResult());
        }else if(entity.getErrorCode() == 107){
            return null;
        }
        throw new Exception(String.valueOf(entity));
    }

    private Integer getTimestamp() {
        return Integer.parseInt(System.currentTimeMillis() / 1000 + "");
    }

    private ResponseEntity entity(String s) {
        if (s == null) {
            return null;
        }

        try {
            System.out.println(URLDecoder.decode(s, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = JSONObject.parseObject(s);
        Integer code = jsonObject.getInteger("errorCode");
        String messgae = jsonObject.getString("errorMessage");
        String result = jsonObject.getString("result");

        ResponseEntity entity = new ResponseEntity();
        entity.setErrorCode(code);
        entity.setErrorMessage(messgae);
        entity.setResult(result);
        return entity;
    }


    @Data
    class ResponseEntity {
        private Integer errorCode;
        private String errorMessage;
        private Object result;
    }

}
