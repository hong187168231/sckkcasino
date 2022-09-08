package com.qianyi.moduleauthenticator;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 网易云盾验证码
 */
public class WangyiDunAuthUtil {

    public static boolean  verify(String validate) {
        String url = "http://c.dun.163.com/api/v2/verify";
        String timestamp = System.currentTimeMillis() + "";
        String nonce = timestamp + CommonUtil.random(5);
        String secretKey = "bdc09f9d58eccd251d95296cbd80dc56";
        Map<String, Object> params = new HashMap<>();
        params.put("captchaId", "9b8a2163bedd48059675adf8856f59e7");
        params.put("validate", validate);
        params.put("user", "");
        params.put("secretId", "3958be95329c3a9a309c7b8f3da6ca46");
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
        if (CommonUtil.checkNull(s)) {
            return false;
        }
        JSONObject jsonObject = JSONObject.parseObject(s);
        Boolean result = jsonObject.getBoolean("result");
        return result;

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

    public static void main(String[] args) {
        String validate = "CN31_Hz88hW12sdc2GZaKRbpxQNOb9L_GXIYv6WJMNrY.Y2NxST-ZCSSIPdDPlmNTuraD-dvXaCFtrErt_FO04GCXpk6Rnf9TRur5-u0e0EYk2T5090.Qc-TZjitVD1NYlfIUUMuLF0dfWasSAnvtwv9NzCey2cf_DYcV8-nSpmkriwxU5JsSn_I1D5Aia9TFvpq6dQgguVKD2MGQsOHe6QEMkdD-LsyfPplAj1XgdsXoegtdGnxrsBCZQlr_V1vVgqSkHJ-QQ.IXLwHK5hYLyNZV.Wu0Dv68Ljacps0nvpnbiMUxiSsPO4ndIWbN8gdsyQZHf9Bh05hkBshAGHSY6KbGRiz4Di2VM7oRE1elPxOY2V-xC6bAZe5Xz-Ps4hp0T-k9th4i87noBek1t0SkKLd-UskcdSB_kDWrLQQOskZKvZCgGSu5U6EPw5OzYHrEkDlP_sGjsX2zCZCsmpkLe6BrzR7iEavB_cRCIlgeQ_DajmHaa7VAy2UB2Ynj58i3";
        boolean verify = WangyiDunAuthUtil.verify(validate);
        if (!verify) {
            System.out.println("验证码错误");
        }
    }

}
