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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemReaderPractiConfiguration {

        private final JobBuilderFactory jobBuilderFactory;

        private final StepBuilderFactory stepBuilderFactory;

        private final DataSource dataSource;



        @Bean
        public Job csvJobT() throws Exception{
            return jobBuilderFactory.get("csvJob")
                    .incrementer(new RunIdIncrementer())
                    .start(this.csvStepT())
                    .next(this.jdbcBatchItemWriterStep())
                    .build();
        }

        @Bean
        @JobScope
        public Step csvStepT() throws Exception {
            return this.stepBuilderFactory
                    .get("csvStep")
                    .<Person, Person>chunk(5)
                    .reader(this.csvReader())
                    .writer(this.csvWriter())
                    .build();

        }

        @Bean
        public Step jdbcBatchItemWriterStep() throws Exception {
                return stepBuilderFactory.get("jdbcBatchItemWriterStep")
                        .<Person,Person>chunk(10)
                        .reader(this.csvReader())
                        .writer(jdbcBatchItemWriter())
                        .build();
        }

    public ItemWriter<? super Person> jdbcBatchItemWriter() {
            JdbcBatchItemWriter<Person> itemWriter =  new JdbcBatchItemWriterBuilder<Person>()
                    .dataSource(dataSource)   //읽을때도 필요하고 쓸때도 필요하다
                    .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                    .sql("insert into person(name, age, address) values(:name, :age, :address)")
                    .build();

            itemWriter.afterPropertiesSet();
            return itemWriter;
    }

    //
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

        public ItemWriter<Person> csvWriter(){
            return items -> {
                    items.forEach(System.out::println);
                System.out.println("-----");
            };
        }

}
