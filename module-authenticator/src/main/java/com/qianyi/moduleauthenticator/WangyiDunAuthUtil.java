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
        String validate = "CN31_ptRi.67PjSH9B4K-0vH-hOZGN-0z-8mazd1M15wJjozIfcjEReopCYOWxrOtKYA7Gavw14uxWuwrNkSAsv8Uh6TqKpsJozugX6XYppQHKU9Ww-T-JcALef8Mt4ntBs6OGfrfUOQYB.iUThrIsF0TfW2j6THREwTuCJtBNkRGDJxliYi.XyRy-sHY4_J_JQ9y1mBmqPmylGSctD4koo-ES_SnpUSE7Vr5H5URRKN-HT_Hft22DRR68TaG9sGvQcQrlNf4fBLtp.C0kvyCqDZ1y5Jy4t_ReHPA5GOtB8UWQ7hDx0X.or-aJzbIAktV.4LPCF6E4MER_1Nc-UacpG60CYTb.0.T.YtAhGzmfEqq9PAzZlWbPKH2SDFhf07kLfJ581i61Y1Q5lMxNzqWOLqVeUClv69DYvdY0y8b75G5yNnNgebIrizXFQkumo0AcqdW0TDNIjTaYJjUZA9m.BLrDgfPZ2xi.CLgIwVUiWSM1_1fT7r4tQcjSCRFjom3";
        boolean verify = WangyiDunAuthUtil.verify(validate);
        System.out.println(verify);
    }

}
