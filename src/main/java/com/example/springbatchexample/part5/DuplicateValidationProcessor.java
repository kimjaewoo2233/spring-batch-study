package com.example.springbatchexample.part5;

import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DuplicateValidationProcessor<T> implements ItemProcessor<T,T> {

    private final Map<String,Object> keyPool = new ConcurrentHashMap<>();
    private final Function<T,String> keyExtractor;
    //받는거 T타입 리턴타입 String
    private final boolean allowDuplicate;

    public DuplicateValidationProcessor(Function<T, String> keyExtractor, boolean allowDuplicate) {
        this.keyExtractor = keyExtractor;
        this.allowDuplicate = allowDuplicate;
    }


    @Override
    public T process(T item) throws Exception {
       if(allowDuplicate){  //true일 경우
           return item;
       }
       String key = keyExtractor.apply(item);   //키를 추출 apply는 T  타입을 받아 R(String) 리턴
        //keyExtractor는 Function타입이고 여기에 Person person = new Person.getName() 을 넣으니 name이 String타입으로 리턴된다.

        if(keyPool.containsKey(key)){   //name이 있을 경우 key를 리턴한다.
            return null;    //중복인경우
        }

        keyPool.put(key,key);
        //
        return item;
    }
}
