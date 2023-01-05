package com.example.springbatchexample.part6;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
//@EnableBatchProcessing
@RequiredArgsConstructor
public class UserConfiguration {

        private final JobBuilderFactory jobBuilderFactory;
        private final StepBuilderFactory stepBuilderFactory;

        private final SaveUserTasklet saveUserTasklet;

        private final EntityManagerFactory entityManagerFactory;

        private final UserRepository userRepository;

        @Bean
        public Job userJob() throws Exception {
            return this.jobBuilderFactory
                    .get("userJob")
                    .incrementer(new RunIdIncrementer())
                    .start(userLevelUpStep())
                    .listener(new LevelUpJobExecutionListener(userRepository).getClass())   //해당 클래스에서 빈주입을 받아 사용하기 때문에 생성자주입필요
                    .build();
        }

        public Step saveUserstep(){
            return this.stepBuilderFactory
                    .get("saveUserStep")
                    .tasklet(saveUserTasklet)
                    .build();
        }

        @Bean
        public Step userLevelUpStep() throws Exception {
                return this.stepBuilderFactory
                        .get("userLevelUpStep")
                        .<User,User>chunk(100)
                        .reader(itemReader())
                        .processor(itemProcessor())
                        .writer(itemWriter())
                        .build();
        }

    private ItemWriter<? super User> itemWriter() {
            return users -> {
                users.forEach(x ->{
                    x.levelUp();
                    userRepository.save(x);
                     }
                );

            };
    }

    private ItemProcessor<? super User,? extends User> itemProcessor() {

                return user ->{
                       if(user.availableLevelUp()) {
                                return user;
                       }
                       //등급 업이 가능하면 user 리턴
                        //그렇지 않다면 null여기서 null리턴하는 건 처리할 데이터가 없다는 것임
                       return null;
                };
        }

        private ItemReader<? extends User> itemReader() throws Exception {
                JpaPagingItemReader<User> page =new JpaPagingItemReaderBuilder<User>()
                        .queryString("select u from User u")
                        .entityManagerFactory(entityManagerFactory)
                        .pageSize(100)  //웬만하면 동일하게
                        .name("userItemReader")
                        .build();

                page.afterPropertiesSet();

                return page;
        }
}
