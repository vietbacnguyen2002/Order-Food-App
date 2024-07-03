package com.bac.se.usermanager.utils;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class GenerateOTP {

    public Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}
