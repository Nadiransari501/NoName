package com.nadir.userAuth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.core.env.Environment;

@SpringBootApplication
public class UserAuthApplication implements CommandLineRunner {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(UserAuthApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Using email: " + environment.getProperty("spring.mail.username"));
        System.out.println("Using pass: " + environment.getProperty("spring.mail.password"));
    }
}
