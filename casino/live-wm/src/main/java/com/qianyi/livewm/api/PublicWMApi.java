package com.qianyi.livewm.api;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@Component
public class PublicWMApi {

    private String url = "https://api.a45.me/api/public/Gateway.php";

    @Value("${project.vendorId}")
    String vendorId;
    @Value("${project.signature}")
    String signature;

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
    public String openGame(String user, String password, Integer lang, String voice, Integer ui) {
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
        params.put("ui", ui);
        params.put("timestamp", timestamp);

        String s = HttpClient4Util.doPost(url, params);

        if (CommonUtil.checkNull(s)) {
            return null;
        }

        ResponseEntity entity = entity(s);
        return (String)entity.getResult();
    }

    public Boolean changeBalance(String user, BigDecimal money,String order,Integer syslang) {
        String cmd = "ChangeBalance";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("money", money.floatValue());
        params.put("order", order);
        params.put("timestamp",timestamp);
        params.put("syslang", syslang);

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
