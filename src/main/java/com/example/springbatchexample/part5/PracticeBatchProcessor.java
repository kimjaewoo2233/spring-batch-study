package com.example.springbatchexample.part5;


import com.example.springbatchexample.part3.Person;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Configuration
@RequiredArgsConstructor
public class PracticeBatchProcessor {

        private final StepBuilderFactory stepBuilderFactory;
        private final JobBuilderFactory jobBuilderFactory;

        private final EntityManagerFactory entityManagerFactory;

        @Bean
        public Job jobPracticeProcessor() throws Exception {
            return jobBuilderFactory.get("jobPracticeProcessor")
                    .incrementer(new RunIdIncrementer())
                    .start(this.stepPracticeTestProcessor(null))
//                    .listener(new SavePersonListener.SavePersonAnnotationJobExecution())    //??????????????? ????????? ????????? ????????? ?????? ???????????? ????????????
//                    .listener(new SavePersonListener.SavePersonJobExecutionListener())
                    .build();
        }

        @Bean
        @JobScope   //jobParameter??? ??????????????? Scope??? ?????? ????????????
        public Step stepPracticeTestProcessor(@Value("#{jobParameters[allow_duplicate]}")String duplicate) throws Exception {
            return stepBuilderFactory
                    .get("stepPracticeTestProcessor")
                    .<Person, Person>chunk(10)
                    .reader(csvPracticeItemReader())
//                    .processor(new DuplicateValidationProcessor<>(Person::getName,
//                            StringUtils.isNotEmpty(duplicate) && Boolean.parseBoolean(duplicate)))
                    .processor(itepProcessor(duplicate))
                    .writer(csvPracticeItemWriter())
                    .faultTolerant()
                    .skip(NotFoundNameException.class)
                    .skipLimit(2)
                    .retry(NotFoundNameException.class)
                    .retryLimit(3)
                    .listener(new SavePersonListener.SavePersonStepExecutionListener())
                    .build();
        }

    private ItemProcessor<? super Person, ? extends Person> itepProcessor(String allowDuplicate) throws Exception {
        DuplicateValidationProcessor<Person> duplicateValidationProcessor=
                new DuplicateValidationProcessor<Person>(Person::getName, Boolean.parseBoolean(allowDuplicate));

        ItemProcessor<Person,Person> validationProcessor = item -> {
                if(item.isNotEmptyName()){
                    return item;
                }

                throw new NotFoundNameException();

        };

        //????????? ItemProcessor??? ??????????????? CompositeItemProcessor??? ???????????? ??????.
        CompositeItemProcessor<Person,Person> itemProcessor = new CompositeItemProcessorBuilder<Person,Person>()
                .delegates(new PersonValidationRetryProcessor() ,duplicateValidationProcessor, validationProcessor)
                .build();

        itemProcessor.afterPropertiesSet();
        return itemProcessor;
    }

    private ItemWriter<Person> csvPracticeItemWriter() throws Exception {
            JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
                    .entityManagerFactory(entityManagerFactory)
                    .usePersist(true)
                    .build();
            //writer??? ?????? ???????????? ?????? ???????????? ??????
           // ItemWriter<Person> logItemWriter = items -> System.out.println("person.size : "+items.size());

            CompositeItemWriter<Person> itemWriter  = new CompositeItemWriterBuilder<Person>()
                    .delegates(jpaItemWriter) //?????? ????????? ?????? ????????? (?????? ????????? ????????????)
                    .build();

            itemWriter.afterPropertiesSet();
            return itemWriter;

        }


    private ItemReader<? extends Person> csvPracticeItemReader() throws Exception {
            DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<Person>();
            //csv ?????? ????????? ?????? ????????? ??????
            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            //csv?????? ????????? ???????????? ????????????.
            tokenizer.setNames("name","age","address");
            lineMapper.setLineTokenizer(tokenizer);


        lineMapper.setFieldSetMapper(fieldSet -> {  //????????? ???????????? ????????????.
            String name = fieldSet.readString("name");
            String age = fieldSet.readString("age");
            String address = fieldSet.readString("address");

            return new Person(name,age,address);
        });

        FlatFileItemReader<Person> itemReader
                = new FlatFileItemReaderBuilder<Person>()
                .name("csvPracticeItemReader")
                .encoding("UTF-8")
                .resource(new ClassPathResource("test-output.csv"))
                .lineMapper(lineMapper)
                .linesToSkip(1)
                .build();

         itemReader.afterPropertiesSet();
            return itemReader;
    }

}
