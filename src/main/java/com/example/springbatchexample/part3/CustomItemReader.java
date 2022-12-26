package com.example.springbatchexample.part3;


import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.List;

public class CustomItemReader<T> implements ItemReader<T> {//Collection 을 Reader로ㅗ 처리 한다.

    private final List<T> items;


    public CustomItemReader(List<T> items){
        this.items = new ArrayList<>();

    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException{
            if(!items.isEmpty()){
                return items.remove(0); //반환하면서 제거함
            }
        //정크반복이 끝이면 null을 리턴한다 결국 리스트에 값이 없으면 null을 리턴해야한다.
         return null;
     }
}
