package com.example.springbatchexample.part3;


import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class PracticePart3 {


        private final JobBuilderFactory jobBuilderFactory;
        private final StepBuilderFactory stepBuilderFactory;

        @Bean
        public Job practiceJob(){
            return jobBuilderFactory.get("practiceJob")
                    .start(this.practiceStep(null))
                    .build();
        }

        @Bean
        @JobScope
        public Step practiceStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {
                return stepBuilderFactory.get("practiceStep")
                        .<String,String>chunk(StringUtils.isNotEmpty(chunkSize) ?Integer.parseInt(chunkSize) : 10)
                        .reader(this.practiceReader())
                        .processor(this.practiceProcessor())
                        .writer(this.practiceWriter())
                        .build();
        }

        @Bean
        public ItemReader<String> practiceReader(){
            return new ListItemReader<>(this.getItems());
        }

        @Bean
        public ItemProcessor<String,String> practiceProcessor(){
            return item -> "Processing + "+item;
        }

        @Bean
        public ItemWriter<String> practiceWriter(){
            return items -> {
                log.info("items size => {}",items.size());
                log.info("items 각 페이지 첫번째 요소 ==> {}",items.get(0));
            };
        }


    private List<String> getItems() {

        List<String> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(i+"Hello");
        }
        return items;

    }

}
