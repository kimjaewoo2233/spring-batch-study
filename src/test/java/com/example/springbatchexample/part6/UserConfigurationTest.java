package com.example.springbatchexample.part6;

import com.example.springbatchexample.config.BatchTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {UserConfiguration.class, BatchTestConfiguration.class,SaveUserTasklet.class})
class UserConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void test() throws Exception {
        JobExecution jobExecution =
                jobLauncherTestUtils.launchJob();

        int size = userRepository
                .findAllByUpdatedDate(LocalDate.now()).size();


        Assertions.assertThat(jobExecution.getStepExecutions().stream()
                        .filter(x -> x.getStepName().equals("userLevelUpStep"))
                        .mapToInt(StepExecution::getWriteCount)
                        .sum())
                .isEqualTo(size)
                .isEqualTo(300);
        //지정한 Step에서 write한 개수를 파악

        //userRepository count는 여기에 저장되어있는 데이터 개수
        Assertions.assertThat(userRepository.count())
                .isEqualTo(400);
    }
}