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
    private final static long ttl = 1 * 60 * 60 * 1000;
    private final static String iss = "dashan";

    /**
     * 生成JWT令牌
     *
     * @return
     */
    public static String generic(String userId) {
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
    public static Claims parse(String token) {
        try {
            Claims body = Jwts.parser()
                    // 验证签发者字段iss 必须是 大山
                    .require("iss", iss)
                    .setSigningKey(secrect)
                    .parseClaimsJws(token)
                    .getBody();
            return body;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean check(String token) {

        try {
            Claims claims = parse(token);
            if (claims == null) {
                return false;
            }
            String subject = claims.getSubject();
            if (ObjectUtils.isEmpty(subject)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
