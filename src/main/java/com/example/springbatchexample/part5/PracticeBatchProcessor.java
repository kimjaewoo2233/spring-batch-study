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
import org.springframework.batch.item.support.CompositeItemWriter;
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
                    .listener(new SavePersonListener.SavePersonAnnotationJobExecution())    //연속적으로 담아도 가능함 위에가 먼저 실행되고 다음아래
                    .listener(new SavePersonListener.SavePersonJobExecutionListener())
                    .build();
        }

        @Bean
        @JobScope   //jobParameter를 사용하려면 Scope가 항상 필요하다
        public Step stepPracticeTestProcessor(@Value("#{jobParameters[allow_duplicate]}")String duplicate) throws Exception {
            return stepBuilderFactory
                    .get("stepPracticeTestProcessor")
                    .<Person, Person>chunk(10)
                    .reader(csvPracticeItemReader())
                    .processor(new DuplicateValidationProcessor<>(Person::getName,
                            StringUtils.isNotEmpty(duplicate) && Boolean.parseBoolean(duplicate)))
                    .writer(csvPracticeItemWriter())
                    .listener(new SavePersonListener.SavePersonStepExecutionListener())
                    .build();
        }

        private ItemWriter<Person> csvPracticeItemWriter() throws Exception {
            JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
                    .entityManagerFactory(entityManagerFactory)
                    .usePersist(true)
                    .build();
            //writer를 두개 만든거임 쓰고 사이즈를 출력
           // ItemWriter<Person> logItemWriter = items -> System.out.println("person.size : "+items.size());

            CompositeItemWriter<Person> itemWriter  = new CompositeItemWriterBuilder<Person>()
                    .delegates(jpaItemWriter) //먼저 실행할 것을 왼쪽에 (순서 주의가 필요하다)
                    .build();

            itemWriter.afterPropertiesSet();
            return itemWriter;

        }


    private ItemReader<? extends Person> csvPracticeItemReader() throws Exception {
            DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<Person>();
            //csv 파일 한줄씩 읽게 해주는 객체
            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            //csv파일 읽을때 식별자를 설정한다.
            tokenizer.setNames("name","age","address");
            lineMapper.setLineTokenizer(tokenizer);


            lineMapper.setFieldSetMapper(fieldSet -> {  //인덱스 번호로도 가능하다.
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
