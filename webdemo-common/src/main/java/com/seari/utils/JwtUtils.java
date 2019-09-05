package com.seari.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author seari
 * @date 2019/9/4
 */
@Component
public class JwtUtils {

    private static final String SUBJECT = "subject";

    @Value("${jwt.secret}")
    public String secret;

    @Value("${jwt.expiredTime}")
    public Long expiredTime;

    /**
     * 创建JWT
     *
     * @param claims playLoad 载荷
     * @return String
     * @throws Exception e
     */
    public String createJWT(Map<String, Object> claims) throws Exception {
        //指定签名的时候使用的签名算法，也就是header那部分，jjwt已经将这部分内容封装好了。
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        //生成JWT的时间
        Date now = new Date();

        //生成签名的时候使用的秘钥secret,这个方法本地封装了的，一般可以从本地配置文件中读取，切记这个秘钥不能外露。它就是你服务端的私钥，在任何场景都不应该流露出去。一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。
        SecretKey key = generalKey();

        //下面就是在为payload添加各种标准声明和私有声明
        //setClaims 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
        //setId 设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
        //setIssuedAt 签发时间
        //setSubject 代表这个JWT的主体，即它的所有人，这个是一个json格式的字符串，可以存放什么userid，roldid之类的，作为什么用户的唯一标志。
        //signWith 设置签名使用的签名算法和签名使用的秘钥
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                //.setId(id)
                .setIssuedAt(now)
                //.setSubject(SUBJECT)
                .signWith(signatureAlgorithm, key);

        //设置token的过期时间
        if (expiredTime != null && expiredTime >= 0) {
            long expMillis = System.currentTimeMillis() + expiredTime * 1000;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //就开始压缩为xxxxxxxxxx.xxxxxxxxxx.xxxxxxxxx这样的jwt
        return builder.compact();

    }

    /**
     * 由字符串生成加密key
     *
     * @return key
     */
    private SecretKey generalKey() {

        byte[] encodedKey = Base64.decodeBase64(secret);
        System.out.println(encodedKey);
        System.out.println(Base64.encodeBase64URLSafeString(encodedKey));

        //根据给定的字节数组使用AES加密算法构造一个密钥，使用 encodedKey中的始于且包含 0 到前 leng 个字节这是当然是所有。（后面的文章中马上回推出讲解Java加密和解密的一些算法）
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;

    }

    /**
     * 解密JWT token
     * 解密使用的key，和生成签名时候的key一样
     * key 可以是string, Key对象
     *
     * @param token token
     * @return claims
     * @throws Exception e
     */
    public Claims parseJWT(String token) {

        SecretKey key = generalKey();
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token).getBody();
        return claims;

    }

    /**
     * 校验JWT token是否过期: true 过期，false 未过期
     *
     * @param token token
     * @return bool
     */
    public boolean checkExpiredTime(String token) {
        Claims claims = parseJWT(token);
        Date expiration = claims.getExpiration();
        if (expiration.before(new Date())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 验证token有效性
     *
     * @param token token
     * @param userId userId
     * @return bool
     */
    public boolean validateToken(String token, String userId) {
        Claims claims = parseJWT(token);
        String userId2 = claims.get("userId", String.class);
        if (userId2.equals(userId) && !checkExpiredTime(token)) {
            return true;
        }
        return false;
    }


}
