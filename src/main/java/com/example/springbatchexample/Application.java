package com.example.springbatchexample;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
@EnableBatchProcessing  //배치 프로세싱 사용
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
