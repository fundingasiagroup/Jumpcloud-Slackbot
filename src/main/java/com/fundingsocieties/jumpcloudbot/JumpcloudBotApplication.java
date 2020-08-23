package com.fundingsocieties.jumpcloudbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class JumpcloudBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(JumpcloudBotApplication.class, args);
    }


}
