package com.atguigu.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

public class jwtTest {

    private static String tokenSignKey = "yangxin";
    private static long tokenExpiration = 24*60*60*1000;



    @Test
    public void testJwtCreateToken(){
        String token = Jwts.builder()
                .setHeaderParam("typ","JWT")
                .setHeaderParam("alg","HS256")
                //默认荷载
                .setSubject("guli-yx")
                .setIssuer("yangyangyang")
                .setAudience("atguigu")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .setNotBefore(new Date(System.currentTimeMillis() + 20*1000))
                .setId(UUID.randomUUID().toString())
                //自定义荷载
                .claim("username","yx")
                .claim("createTime","2021.9.28")
                .claim("age","23")
//                .signWith(SignatureAlgorithm.HS256,tokenSignKey)
                .signWith(SignatureAlgorithm.HS256,tokenSignKey + tokenExpiration)
                .compact();

        System.out.println(token);

    }

    @Test
    public void testJwtDe(){
        String token1 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJndWxpLXl4IiwiaXNzIjoieWFuZ3lhbmd5YW5nIiwiYXVkIjoiYXRndWlndSIsImlhdCI6MTYzMjgyMzAzMywiZXhwIjoxNjMyOTA5NDMzLCJuYmYiOjE2MzI4MjMwNTMsImp0aSI6Ijc2Zjc5N2RiLTQ3ODAtNGQyOC1hYzFkLWYyMzU3ZTcxNjEwMiIsInVzZXJuYW1lIjoieXgiLCJjcmVhdGVUaW1lIjoiMjAyMS45LjI4IiwiYWdlIjoiMjMifQ.wBoZhKEiur6XnKxtGcOypO69n81_6nGBVy8JjD70SAk";
        String token2 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJndWxpLXl4IiwiaXNzIjoieWFuZ3lhbmd5YW5nIiwiYXVkIjoiYXRndWlndSIsImlhdCI6MTYzMjgyMzQxNywiZXhwIjoxNjMyOTA5ODE3LCJuYmYiOjE2MzI4MjM0MzcsImp0aSI6IjkyMzYyM2Q2LTBmZTItNGQzMS05NDNiLWYxM2VlODE2ZjIxMSIsInVzZXJuYW1lIjoieXgiLCJjcmVhdGVUaW1lIjoiMjAyMS45LjI4IiwiYWdlIjoiMjMifQ.16lvNqE5YEh3Dl0UUvTlNKi9DC9zyIdOJZCeOwZ4xRM";
//        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token1);
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey + tokenExpiration).parseClaimsJws(token2);
        Claims body = claimsJws.getBody();
        System.out.println(claimsJws);
        System.out.println(body);
    }


}
