package com.example.springbatchexample.part3;


import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ChunkProcessingConfiguration {


        private final JobBuilderFactory jobBuilderFactory;

        private final StepBuilderFactory stepBuilderFactory;

        //--spring.batch.job.names=chunkProcessingJob
        @Bean
        public Job chunkProcessingJob(){
                return jobBuilderFactory.get("chunkProcessingJob")
                        .incrementer(new RunIdIncrementer())
                        .start(this.taskBaseStep())
                        .next(this.chunkBaseStep(null))     //chunk는 next로
                        .build();
        }

        @Bean
        public Step taskBaseStep(){
                        return stepBuilderFactory
                                .get("taskBaseStep")
                                .tasklet(this.tasklet(null))
                                .build();
        }


        @Bean
        @JobScope
        public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String chunkSize){

                //100 개에 데이터를 10번씩 10번 나눈다
                return stepBuilderFactory.get("chunkBaseStep")
                        .<String,String>chunk(StringUtils.isNotEmpty(chunkSize) ? Integer.parseInt(chunkSize) : 10)//총 데이터를 10개씩 나눈다는 의미 총데이터가 100개이미르 정크가 10번실행
                        .reader(itemReader())
                        .processor(itemProcessor())
                        .writer(itemWriter())
                        .build();
        }

        private ItemWriter<? super String> itemWriter() {
                return items -> log.info("chunk item size : {}",items.size());
        }

        //ItemProcessor는 Reader에서 읽은 데이터를 가공하거나 Wrtier로 넘길지 말지를 결정한다
        //만약 Processor에서 null로 리턴이 된다면 해당 item은 writer로 넘어갈 수 없게 된다
        private ItemProcessor<? super String, String> itemProcessor() {
                return item -> item + ", Spring Batch";
                //reader에서 읽은 item에 처리한다.
        }

        private ItemReader<String> itemReader() {
                return new ListItemReader<>(getItems()); //생성자로 리스트를 받아서 처리할 수 있는 itemReader
        }


  //      @Bean
//        @StepScope      //StepExecution에서 잡파라미터객체를 꺼내서 정크 사이즈 파라미터를 꺼냈었는데 이번에는 정크 베잇 스탭과 마찬가지로 EL로 변경할거임
//        public Tasklet tasklet() {
//                List<String> items = getItems();
////Tasklet 페이징처리하기 나이스하지 못해서 Chunk가 나음
//                return ((contribution, chunkContext) -> {
//                        StepExecution stepExecution = contribution.getStepExecution();
//                        //StepExecution은 읽은 아이템 크기를 저장할 수 있는데 저장할 수 있으니까
//                        //당연히 조회도 가능하다
//                        //StepExecution에서 JobParameter 객체를 꺼낼 수 있다.
//                        //jobParameter에서 정크사이즈를 꺼내고 없으면 10으로 지정한다.
//                        JobParameters jobParameters=stepExecution.getJobParameters();
//
//                        //value는 parameter중 chunkSize를 꺼내는데 만약 없는 경우 10을 넣음
//                        String value = jobParameters.getString("chunkSize","10");
//                        int chunkSize = StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : 10;
//                        //null일 경우 10 있다면 형변환
//                        //이렇게 파라미터를 사용하면 배치 실행시에 값을 변경할 수 있다'
//                        int fromIndex = stepExecution.getReadCount();   //정크에서 읽은 아이템크기
//                        int toIndex = fromIndex + chunkSize;    //fromIndex부터 chunkSize까지 읽을 수 이씅ㅁ
//
//                        if(fromIndex >= items.size()){
//                                return RepeatStatus.FINISHED;
//                        }
//
//                        List<String> subList = items.subList(fromIndex,toIndex);
//                        log.info("task item size: {}",subList.size());
//                        stepExecution.setReadCount(toIndex);
//                        return RepeatStatus.CONTINUABLE;        //Tasklet을 반복해서 처리하라는뜻임
//                });
//        }
        @Bean
        @StepScope      //StepExecution에서 잡파라미터객체를 꺼내서 정크 사이즈 파라미터를 꺼냈었는데 이번에는 정크 베잇 스탭과 마찬가지로 EL로 변경할거임
        public Tasklet tasklet(@Value("#{jobParameters[chunkSize]}") String value) {
                List<String> items = getItems();
//Tasklet 페이징처리하기 나이스하지 못해서 Chunk가 나음
                return ((contribution, chunkContext) -> {
                        StepExecution stepExecution = contribution.getStepExecution();
                        //StepExecution은 읽은 아이템 크기를 저장할 수 있는데 저장할 수 있으니까
                        //당연히 조회도 가능하다
                        //StepExecution에서 JobParameter 객체를 꺼낼 수 있다.
                        //jobParameter에서 정크사이즈를 꺼내고 없으면 10으로 지정한다.
                        JobParameters jobParameters=stepExecution.getJobParameters();

                        //value는 parameter중 chunkSize를 꺼내는데 만약 없는 경우 10을 넣음
                    //    String value = jobParameters.getString("chunkSize","10");
                        int chunkSize = StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : 10;
                        //null일 경우 10 있다면 형변환
                        //이렇게 파라미터를 사용하면 배치 실행시에 값을 변경할 수 있다'
                        int fromIndex = stepExecution.getReadCount();   //정크에서 읽은 아이템크기
                        int toIndex = fromIndex + chunkSize;    //fromIndex부터 chunkSize까지 읽을 수 이씅ㅁ

                        if(fromIndex >= items.size()){
                                return RepeatStatus.FINISHED;
                        }

                        List<String> subList = items.subList(fromIndex,toIndex);
                        log.info("task item size: {}",subList.size());
                        stepExecution.setReadCount(toIndex);
                        return RepeatStatus.CONTINUABLE;        //Tasklet을 반복해서 처리하라는뜻임
                });
        }

        private List<String> getItems() {

                List<String> items = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                        items.add(i+"Hello");
                }
                return items;

        }
}
