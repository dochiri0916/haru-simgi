package com.dochiri.habitservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HabitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabitServiceApplication.class, args);
    }

}
