package com.example.springbatchexample.part4;


import com.example.springbatchexample.part3.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class JpaItemJobTest {

        private final JobBuilderFactory jobBuilderFactory;
        private final StepBuilderFactory stepBuilderFactory;

        private final EntityManagerFactory entityManagerFactory;


        @Bean
        public Job jpaReaderJob() throws Exception {
            return jobBuilderFactory
                    .get("jpaReaderJob")
                    .incrementer(new RunIdIncrementer())
                    .start(this.jpaStep2())
                    .build();
        }

        @Bean
        @JobScope
        public Step jpaStep2() throws Exception {
            return stepBuilderFactory
                    .get("jpaReaderStep")
                    .<Person, Person>chunk(20)
                    .reader(this.jpaItemReader())
                    .writer(this.jpaItemWriter())
                    .build();
        }

    private JpaPagingItemReader<Person> jpaItemReader() throws Exception {

        JpaPagingItemReader<Person> itemReader = new JpaPagingItemReaderBuilder<Person>()
                .name("jpaItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(10)
                .queryString("select p from Person p where p.name like :name ")
                .parameterValues(Collections.singletonMap("name","%test name%"))
                .build();

        itemReader.afterPropertiesSet();

        return itemReader;
    }

    private ItemWriter<Person> jpaItemWriter() {

                return items -> {
                    items.forEach(System.out::println);
                    System.out.println("=====================");
                };
        }
}
