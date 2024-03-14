package com.neil.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GitManageApplication {

    private String test;

    public static void main(String[] args) {
        SpringApplication.run(GitManageApplication.class, args);

    }

}
