package com.bac.se.usermanager.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JWTService {

    private static final String ACCESS_SECRET_KEY = "BF7FD11ACE545745B7BA1AF98B6F156D127BC7BB544BAB6A4FD74E4FC7";
    private static final String REFRESH_SECRET_KEY="A17FD11ACE545745B7BA1AF9116F156D127BC7BB544BAB6A4FD74E4FC7";

    // extract username from JWT
    public String extractUsername(String token,String secretKey) {
        return extractClaim(token, Claims::getSubject,secretKey);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver,String secretKey) {
        final Claims claims = extractAllClaims(token,secretKey);
        return claimsResolver.apply(claims);
    }

    // extract information from JWT
    private Claims extractAllClaims(String token,String secretKey) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // decode and get the key
    private Key getSignInKey(String secretKey) {
        // decode SECRET_KEY
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


//    public String generateToken(UserDetails userDetails) {
//        return generateToken(new HashMap<>(), userDetails);
//    }

    // generate token using Jwt utility class and return token as String
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            String secretKey,
            long expired
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expired * 1000))
                .signWith(getSignInKey(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }

    // if token is valid by checking if token is expired for current user
    public boolean isTokenValid(String token, UserDetails userDetails,String secretKey) {
        final String username = extractUsername(token,secretKey);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token,secretKey));
    }

    // if token is expired
    private boolean isTokenExpired(String token,String secretKey) {
        return extractExpiration(token,secretKey).before(new Date());
    }

    // get expiration date from token
    private Date extractExpiration(String token,String secretKey) {
        return extractClaim(token, Claims::getExpiration,secretKey);
    }

}
