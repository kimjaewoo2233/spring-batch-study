package com.example.springbatchexample.part3;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemReaderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory entityManagerFactory;
    //스프링부트에서는 자동으로 EntitiyManager도 만들어주기때문에 빈주입만 쓰면도미



    @Bean
    public Job itemReaderJob() throws Exception {
        return jobBuilderFactory
                .get("itemReaderJob")
                .incrementer(new RunIdIncrementer())
                .start(this.jpaStep())
                .build();
    }

    @Bean
    public Step customItemReaderStep(){
        return stepBuilderFactory.get("customItemReaderStep")
                .<Person,Person>chunk(10)
                .reader(new CustomItemReader<>(getItems())) //리더로 처리할 데이터 대상
                .writer(itemWriter())
                .build();
    }


    @Bean
    public Step csvFileStep() throws Exception {
        return stepBuilderFactory.get("csvFileStep")
                .<Person,Person>chunk(10)
                .reader(this.csvFileItemReader())
                .writer(itemWriter())
                .build();

    }

    //csv파일읽기
    private FlatFileItemReader<Person> csvFileItemReader() throws Exception {

        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id","name","age","address");
        lineMapper.setLineTokenizer(tokenizer);

        lineMapper.setFieldSetMapper(fieldSet -> {
           int id =  fieldSet.readInt("id");
           String name = fieldSet.readString("name");
           String age = fieldSet.readString("age");
           String address = fieldSet.readString("address");

           return new Person(id,name,age,address);
        });
        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
                .name("csvFileItemReader")
                .encoding("UTF-8")
                .resource(new ClassPathResource("test.csv"))
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .build();

        itemReader.afterPropertiesSet();
        return itemReader;
    }
    private ItemWriter<? super Person> itemWriter() {

        return items -> {
            log.info(items
                    .stream()
                    .map(Person::getName)
                    .collect(Collectors.joining(", ")));
        };

    }



    @Bean
    @JobScope
    public Step jpaStep() throws Exception {
        return stepBuilderFactory
                .get("jpaStep")
                .<Person,Person>chunk(10)
                .reader(this.jpaCursorItemReader())
                .writer(this.itemWriter2())
                .build();
    }
    private ItemWriter<Person> itemWriter2(){
        return items -> {
            System.out.println("데이터");
            System.out.println(items.stream().map(Person::getName));
        };
    }
    private JpaCursorItemReader<Person> jpaCursorItemReader() throws Exception {
            JpaCursorItemReader<Person> itemReader =
                    new JpaCursorItemReaderBuilder<Person>()
                    .name("jpaCursorItemReader")
                    .entityManagerFactory(entityManagerFactory)
                    .queryString("select p from Person p")
                    .build();

            itemReader.afterPropertiesSet();

            return itemReader;




    }


    private List<Person> getItems(){
        List<Person> items = new ArrayList<>();
        IntStream.rangeClosed(1,10).forEach(value -> {
            items.add(new Person(value+1,"test name"+value,"test age","test addres"));
        });
        return items;
    }
}
