package com.example.springbatchexample.part5;

import com.example.springbatchexample.part3.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

public class PersonValidationRetryProcessor implements ItemProcessor<Person, Person> {

    private final RetryTemplate retryTemplate;

        public PersonValidationRetryProcessor(){
            this.retryTemplate = new RetryTemplateBuilder()
                    .maxAttempts(3)  //retryLimit과 비슷
                    .retryOn(NotFoundNameException.class)
                    .build();
        }

    @Override
    public Person process(Person item) throws Exception {
        return this.retryTemplate.execute(context -> {
                    //Retry Callback
                //process메소드가 호출되면 실행되는 처음 시작점
                if(item.isNotEmptyName ()){
                    return item;
                }
                throw new NotFoundNameException();
        },context -> {
                    //RecoveryCallBack
            return item.unknownName();
        });
    }
}
