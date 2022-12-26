package com.example.springbatchexample.part3;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class TestData {
        private String name;
        private String road;
        private String call;
        private String total;

}

