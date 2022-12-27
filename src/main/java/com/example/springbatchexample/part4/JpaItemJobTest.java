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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

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
                    .start(this.jpaItemWriterStep())
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

        @Bean
        public Step jpaItemWriterStep() throws Exception {
            return stepBuilderFactory.get("jpaItemWriterStep")
                    .<Person,Person>chunk(10)
                    .reader(csvReader())
                    .writer(jpaDifferWriter())
                    .build();
        }

        public ItemWriter<Person> jpaDifferWriter() throws Exception {
            JpaItemWriter<Person> itemWriter = new JpaItemWriterBuilder<Person>()
                    .entityManagerFactory(entityManagerFactory)
                    .usePersist(true)
                    .build();

            itemWriter.afterPropertiesSet();

            return itemWriter;
        }

        public FlatFileItemReader<Person> csvReader() throws Exception {

            DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            tokenizer.setNames("id","name","age","address");
            lineMapper.setLineTokenizer(tokenizer);

            lineMapper.setFieldSetMapper(fieldSet -> {
                int id = fieldSet.readInt("id");
                String name = fieldSet.readString("name");
                String age = fieldSet.readString("age");
                String address = fieldSet.readString("address");

                return new Person(id,name,age,address);

            });

            FlatFileItemReader<Person> itemReader =
                    new FlatFileItemReaderBuilder<Person>()
                            .name("csvItemReader")
                            .encoding("UTF-8")
                            .resource(new ClassPathResource("test-output.csv"))
                            .linesToSkip(2)
                            .lineMapper(lineMapper)
                            .build();
            itemReader.afterPropertiesSet();

            return itemReader;

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
