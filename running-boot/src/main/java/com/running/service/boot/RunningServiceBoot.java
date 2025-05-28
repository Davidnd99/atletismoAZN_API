package com.running.service.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.running.repository")
@EntityScan(basePackages = "com.running.model")
@ComponentScan(basePackages = "com.running")
public class RunningServiceBoot {
    public static void main(String[] args) {
        SpringApplication.run(RunningServiceBoot.class, args);
    }
}