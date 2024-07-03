package com.bac.se.usermanager;

import com.bac.se.usermanager.models.User;
import com.bac.se.usermanager.services.UserService;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagerApplication.class, args);
    }

    //    @Autowired
//    UserService userService;
    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            String refreshTokenExpired = System.getenv("REFRESH_TOKEN_EXPIRED");
            System.out.println(refreshTokenExpired);
        };
    }

}
