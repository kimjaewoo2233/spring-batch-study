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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TestDataConfiguration {

        private final JobBuilderFactory jobBuilderFactory;

        private final StepBuilderFactory stepBuilderFactory;

        private final DataSource dataSource;    //yml에서 설정안 드라이버로 만들어진게 ㅏ자동 주입



        private final EntityManagerFactory entityManagerFactory;

        @Bean
        public Job job() throws Exception {
            return jobBuilderFactory.get("jobPractice")
                    .incrementer(new RunIdIncrementer())
                    .start(this.jdbcStep())
                    .build();
        }

        @Bean
        @JobScope
        public Step step() throws Exception {
                return stepBuilderFactory
                        .get("stepPractice")
                        .<TestData,TestData>chunk(5)
                        .reader(this.csvFileItemReader1())
                        .writer(this.writerData())
                        .build();
        }
        @StepScope
        public ItemWriter<TestData> writerData(){
                return items ->{
                        items.forEach(System.out::println);
                };
        }

        private JdbcCursorItemReader<Person> jdbcCursorItemReader() throws Exception {
             JdbcCursorItemReader<Person> itemReader = new JdbcCursorItemReaderBuilder<Person>()
                        .name("jdbcCursorItemReader")
                        .dataSource(dataSource)
                        .sql("select id, name, age, address from person")
                        .rowMapper((rs, rowNum) ->
                                new Person(rs.getInt(1),
                                rs.getString(2)
                                ,rs.getString(3)
                                ,rs.getString(4)))
                        .build();
             itemReader.afterPropertiesSet();
             return itemReader;
        }


        @Bean
        public Step jdbcStep() throws Exception {
                        return stepBuilderFactory.get("jdbcStep")
                                .<Person,Person>chunk(20)
                                .reader(this.jpaPagingItemReader())
                                .writer(itemWriter()).build();
        }


        @StepScope
        private FlatFileItemReader<TestData> csvFileItemReader1() throws Exception {

                DefaultLineMapper<TestData> lineMapper = new DefaultLineMapper<>();
                DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
                tokenizer.setNames("name","road","call","total");
                lineMapper.setLineTokenizer(tokenizer);

                lineMapper.setFieldSetMapper(fieldSet -> {

                        String name = fieldSet.readString("name");
                        String road = fieldSet.readString("road");
                        String call = fieldSet.readString("call");
                        String total = fieldSet.readString("total");

                        return TestData.builder().name(name).road(road).call(call).total(total).build();
                });


                FlatFileItemReader<TestData> itemReader =
                        new FlatFileItemReaderBuilder<TestData>()
                                .name("csvFileItemReader1")
                                .encoding("UTF-8")
                                .resource(new ClassPathResource("practice.csv"))
                                .linesToSkip(1)
                                .lineMapper(lineMapper)
                                .build();

                itemReader.afterPropertiesSet();
                return itemReader;
        }

        public JpaPagingItemReader<Person> jpaPagingItemReader(){

                return new JpaPagingItemReaderBuilder<Person>()
                        .name("jpaItemReader")
                        .entityManagerFactory(entityManagerFactory)
                        .queryString("select p from Person p")
                        .pageSize(10)
                        .build();
        }
        private ItemWriter<? super Person> itemWriter() {

                return items -> {
                        log.info(items
                                .stream()
                                .map(Person::getName)
                                .collect(Collectors.joining(", ")));
                };

        }
}
