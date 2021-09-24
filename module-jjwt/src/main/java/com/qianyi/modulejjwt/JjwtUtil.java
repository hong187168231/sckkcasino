package com.qianyi.modulejjwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

public class JjwtUtil {

    private final static String secrect = "fda#$&%$3t55v785A45DF$^&#*JGRstTRG";
    //        private final static long ttl = 5 * 60 * 1000;
    private final static long ttl = 24 * 60 * 60 * 1000;
    private final static Long refresh_ttl = 30 * 60L;//秒
    private final static String iss = "dashan";

    public static String generic(Subject subject) {
        if (ObjectUtils.isEmpty(subject) || ObjectUtils.isEmpty(subject.getUserId())
                || ObjectUtils.isEmpty(subject.getBcryptPassword())) {
            return null;
        }
        return genericJwt(subject, ttl);
    }

    /**
     * 生成JWT令牌
     *
     * @return
     */
    private static String genericJwt(Subject subject, long ttl) {

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        String sub = JSON.toJSONString(subject);
        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID() + "")
                // 头部
                .setHeaderParam("typ", "JWT")
                .setSubject(sub)
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
    public static Subject parse(String token) {
        if (token == null) {
            return null;
        }
        try {
            Claims body = Jwts.parser()
                    // 验证签发者字段iss 必须是 大山
                    .require("iss", iss)
                    .setSigningKey(secrect)
                    .parseClaimsJws(token)
                    .getBody();
            String json = body.getSubject();
            if (ObjectUtils.isEmpty(json)) {
                return null;
            }

            Subject subject = JSON.parseObject(json, Subject.class);
            return subject;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean check(String token) {

        try {
            Subject subject = parse(token);
            if (ObjectUtils.isEmpty(subject)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String refreshToken(String token, String bcryptPassword) {
        return refreshToken(token, bcryptPassword, ttl);
    }

    private static String refreshToken(String token, String bcryptPassword, Long ttl) {
        if (ObjectUtils.isEmpty(token) || ObjectUtils.isEmpty(bcryptPassword)) {
            return null;
        }
        try {
            Long exp = getExp(token);
            if (exp == null) {
                return null;
            }
            String iss = getIss(token);
            if (!(JjwtUtil.iss.equals(iss))) {
                return null;
            }

            Subject subject = getSubject(token);
            if (subject == null) {
                return null;
            }

            String bcrypt = subject.getBcryptPassword();
            if (!(bcryptPassword.equals(bcrypt))) {
                return null;
            }

            Long now = System.currentTimeMillis();
            Long diff = now / 1000 - exp;
            if (diff > refresh_ttl) {
                return null;
            }

            return generic(subject);

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

    public static Subject getSubject(String token) {

        try {
            JSONObject jsonObject = decodeBase64(token);
            JSONObject sub = jsonObject.getJSONObject("sub");
            Subject subject = new Subject();
            subject.setUserId(sub.getString("userId"));
            subject.setBcryptPassword(sub.getString("bcryptPassword"));
            return subject;
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
        if (split.length < 1) {
            return null;
        }
        String tokenBody = split[1];
        byte[] bytes = Base64.decodeBase64(tokenBody);
        String decode = new String(bytes, "utf-8");
        JSONObject jsonObject = JSONObject.parseObject(decode);
        return jsonObject;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        Subject subject = new Subject();
        subject.setUserId("1");
        subject.setBcryptPassword("aadaa");

        String token = generic(subject);
        System.out.println(token);
//        String parse = parse(token);
//        System.out.println(parse);
//
//        boolean check = check(token);
//        System.out.println(check);
//
//        String s = refreshToken(token, subject.getBcryptPassword());
//        System.out.println(s);

//        Long ext = JjwtUtil.getExp(token);
//        System.out.println("======:" + ext);
    }


    @Data
    public static class Subject {

        private String userId;
        private String bcryptPassword;
    }

}

