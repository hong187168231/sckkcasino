package com.qianyi.modulejjwt;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JjwtUtil {

    private final static String secrect = "fda#$&%$3t55v785A45DF$^&#*JGRstTRG";
    private static long ttl = 1 * 60 * 60 * 1000;
    private final static String iss = "dashan";

    //开发用。不可用于正式项目
    public static String generic(String userId,long testTtl) {
       return genericJwt(userId,testTtl);
    }

    public static String generic(String userId) {
        return genericJwt(userId,ttl);
    }

    /**
     * 生成JWT令牌
     *
     * @return
     */
    private static String genericJwt(String userId,long ttl) {
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

   /* public static void main(String[] args) {
        String generic = JjwtUtil.generic("1",3*24 * 60 * 60 * 1000);
        System.out.println(generic);
        String subject = JjwtUtil.parse(generic);
        System.out.println(subject);

    }*/
}
