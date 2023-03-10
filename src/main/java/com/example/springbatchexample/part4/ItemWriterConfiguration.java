package com.example.springbatchexample.part4;

import com.example.springbatchexample.part3.CustomItemReader;
import com.example.springbatchexample.part3.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemWriterConfiguration {

        private final JobBuilderFactory jobBuilderFactory;
        private final StepBuilderFactory stepBuilderFactory;


        @Bean
        public Job itemWriterJob() throws Exception {
                return this.jobBuilderFactory
                        .get("itemWriterJob")
                        .incrementer(new RunIdIncrementer())
                        .start(this.csvItemWriterStep())
                        .build();
            }

    @Bean
    public Step csvItemWriterStep() throws Exception {
        return this.stepBuilderFactory.get("csvItemWriterStep")
                .<Person, Person>chunk(10)
                .reader(itemReader())
                .writer(csvFileItemWriter())
                .build();
    }

    private ItemWriter<? super Person> csvFileItemWriter() throws Exception {
            //csv ????????? ?????? ???????????? ???????????? ????????? fieldExtracter??? ????????????
        BeanWrapperFieldExtractor<Person> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id","name","age","address"});
        //???????????? ???????????? ??? ????????? ???????????? ?????????????????? ?????? DelimitedLineAggregator??? ??????????????????
        DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");       //csv ??????????????? , ???????????? ????????????.
        lineAggregator.setFieldExtractor(fieldExtractor);   //????????? ?????? FieldExtractor??? lineAggregator??? ??????

        FlatFileItemWriter<Person> itemWriter = new FlatFileItemWriterBuilder<Person>()
                .name("csvFileItemWriter")
                .encoding("UTF-8")
                .resource(new FileSystemResource("src/main/resources/test-data.csv"))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("id,??????,??????,?????????"))
                .append(true)
                .build();


        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    private ItemReader<Person> itemReader() {
        return new ListItemReader<>(getItems());
    }

            private List<Person> getItems() {
                List<Person> items = new ArrayList<>();

                for (int i = 0; i < 100; i++) {
                    items.add(new Person("test name" + i, "test age", "test address"));
                }

                return items;
            }

}
