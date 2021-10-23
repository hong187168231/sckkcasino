package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.BukaHttpClient4Util;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.TreeMap;

@RestController
@RequestMapping("messageBalance")
@Api(tags = "运营中心")
public class MessageBalanceController {

    private final static String url_half = "https://api.onbuka.com/v3/";

    @PostMapping("balance")
    @ApiOperation("查询短信平台余额")
    public ResponseEntity balance() {
        String url = getUrl("getBalance");
        String result = BukaHttpClient4Util.doGet(url);
        System.out.println(result);

        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            Integer status = jsonObject.getInteger("status");
            if (status != 0) {
                String message = jsonObject.getString("reason");
                return ResponseUtil.custom(message);
            }

            String balance = jsonObject.getString("balance");
            return ResponseUtil.success(balance);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.fail();
        }
    }

    private boolean checkMerchant(String merchant) {

        switch (merchant) {
            //casino项目
            case "js":
                return true;
        }
        return false;
    }

    private String getContent(String merchant, Integer language, String code) {
        String content = "【" + merchant + "】" + " 您的验证码为:" + code;
        switch (language) {
            case 2:
                content = "【" + merchant + "】" + "verification code:" + code;
                break;
            case 3:
                content = "【" + merchant + "】" + "កូដ ផ្ទៀង ផ្ទាត់ :" + code;
                break;
            default:
        }
        return content;
    }

    private boolean checkCountry(String country) {
        if (country == null) {
            country = "86";
        }

        switch (country) {
            case "86":
            case "855":
                return true;
        }

        return false;
    }

    private boolean checkLanguage(Integer language) {
        if (language == null) {
            language = 1;
        }

        switch (language) {
            case 1:
            case 2:
            case 3:
                return true;
        }
        return false;
    }

    private String getUrl(String apiName) {
        return url_half.concat(apiName);
    }

}
