package com.qianyi.moduleauthenticator;

import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import com.sun.deploy.net.HttpUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 网易云盾验证码
 */
public class WangyiDunAuthUtil {

    public static void verify(String validate) {
        String url = "http://c.dun.163.com/api/v2/verify";
        String timestamp = System.currentTimeMillis() + "";
        String nonce = timestamp + CommonUtil.random(5);
        String secretKey = "0827adaf67d2c4014c55e86c92104274";
        Map<String, Object> params = new HashMap<>();
        params.put("captchaId", "a2eb62e9d6be4d4e945d5a403285b229");
        params.put("validate", validate);
        params.put("user", "");
        params.put("secretId", "fb00c2047073fc009ec0135df4f5d33f");
        params.put("version", "v2");
        params.put("timestamp", timestamp);
        params.put("nonce", nonce);

        try {
            String signature = genSignature(secretKey, params);
            params.put("signature", signature);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String s = HttpClient4Util.doPost(url, params);
        System.out.println(s);
    }

    private static String genSignature(String secretKey, Map<String, Object> params) throws UnsupportedEncodingException {
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 2. 按照排序拼接参数名与参数值
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append(params.get(key));
        }
        // 3. 将secretKey拼接到最后
        sb.append(secretKey);

        // 4. MD5是128位长度的摘要算法，转换为十六进制之后长度为32字符
        return DigestUtils.md5Hex(sb.toString().getBytes("UTF-8"));
    }

}
