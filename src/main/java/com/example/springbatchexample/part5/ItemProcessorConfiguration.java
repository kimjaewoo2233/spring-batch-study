package com.example.springbatchexample.part5;


import com.example.springbatchexample.part3.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ItemProcessorConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job jobProcessor(){
        return this.jobBuilderFactory
                .get("itemProcessorStep")
                .incrementer(new RunIdIncrementer())
                .start(stepProcessor())
                .build();
    }

    @Bean
    public Step stepProcessor() {
            return stepBuilderFactory
                    .get("itemProcessorStep")
                    .<Person, Person>chunk(10)
                    .reader(processorReader())
                    .processor(processorProcessing())
                    .writer(processorWriter())
                    .build();

    }

    private ItemWriter<? super Person> processorWriter() {
            return items -> {
                items.forEach(System.out::println);
            };
    }

    private ItemProcessor<? super Person, ? extends Person> processorProcessing() {


        return item -> {    // 여기서 item은 List에 Person객체
                if(item.getId() % 2 == 0){
                    return item;
                }
                return null;
        };
    }

    private ItemReader<? extends Person> processorReader() {
        return new ListItemReader<Person>(getItems());
    }
    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            items.add(new Person("test name" + i, "test age", "test address"));
        }

        return items;
    }

}
