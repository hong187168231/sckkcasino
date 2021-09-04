package com.qianyi.modulejjwt;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

public class JjwtUtil {

    private final static String secrect = "fda#$&%$3t55v785A45DF$^&#*JGRstTRG";
//    private final static long ttl = 5 * 60 * 1000;
private final static long ttl = 24*60 * 60 * 1000;
    private final static Long refresh_ttl = 30 * 60L;//秒
    private final static String iss = "dashan";

    public static String generic(String userId) {
        return genericJwt(userId, ttl);
    }

    /**
     * 生成JWT令牌
     *
     * @return
     */
    private static String genericJwt(String userId, long ttl) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID() + "")
                // 头部
                .setHeaderParam("typ", "JWT")
                .setSubject(userId)
                //用于设置签发时间
                .setIssuer(iss)
                .setIssuedAt(now)
                .setExpiration(new Date(nowMillis + ttl))
                //用于设置签名秘钥
                .signWith(SignatureAlgorithm.HS256, secrect);
        return builder.compact();
    }

    /**
     * 解析TOKEN
     *
     * @param token
     * @return
     */
    public static String parse(String token) {
        try {
            Claims body = Jwts.parser()
                    // 验证签发者字段iss 必须是 大山
                    .require("iss", iss)
                    .setSigningKey(secrect)
                    .parseClaimsJws(token)
                    .getBody();
            return body.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean check(String token) {

        try {
            String subject = parse(token);
            if (ObjectUtils.isEmpty(subject)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String refreshToken(String token) {
        return refreshToken(token, ttl);
    }

    public static String refreshToken(String token, Long ttl) {
        try {
            Long exp = getExp(token);
            if (exp == null) {
                return null;
            }
            String iss = getIss(token);
            if (!(JjwtUtil.iss.equals(iss))) {
                return null;
            }

            String subject = getSubject(token);
            if (subject == null) {
                return null;
            }

            Long now = System.currentTimeMillis();
            Long diff = now / 1000 - exp;
            if (diff > refresh_ttl) {
                return null;
            }

            return genericJwt(subject, ttl);

        } catch (Exception e) {
            return null;
        }
    }

    private static Long getExp(String token) {

        try {
            JSONObject jsonObject = decodeBase64(token);
            return jsonObject.getLong("exp");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static String getSubject(String token) {

        try {
            JSONObject jsonObject = decodeBase64(token);
            return jsonObject.getString("sub");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getIss(String token) {

        try {
            JSONObject jsonObject = decodeBase64(token);
            return jsonObject.getString("iss");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject decodeBase64(String token) throws Exception {
        if (token == null) {
            return null;
        }

        String[] split = token.split("\\.");
        String tokenBody = split[1];
        byte[] bytes = Base64.decodeBase64(tokenBody);
        String decode = new String(bytes, "utf-8");
        JSONObject jsonObject = JSONObject.parseObject(decode);
        return jsonObject;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxNmUzZTFkMi0zOTY2LTRjMGQtYmNmOS1mYWVhNmRhNzI4NDEiLCJzdWIiOiIxIiwiaXNzIjoiZGFzaGFuIiwiaWF0IjoxNjI5OTUyMzgxLCJleHAiOjE2MzAyMTE1ODF9.RtBHmfCQU1Hu-AiQ9_OOqeb38DCrjeygBpNmxJClhlc";

        Long ext = JjwtUtil.getExp(token);
        System.out.println("======:" + ext);
    }
}
