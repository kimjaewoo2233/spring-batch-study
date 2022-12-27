package com.example.springbatchexample.config;

import com.example.springbatchexample.TestConfiguration;
import com.example.springbatchexample.part3.PersonRepository;
import com.example.springbatchexample.part5.PracticeBatchProcessor;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PracticeBatchProcessor.class, TestConfiguration.class})
public class SavePersonConfigurationTest {


        @Autowired
        private JobLauncherTestUtils jobLauncherTestUtils;

        @Autowired
        private PersonRepository personRepository;

        @After
        public void tearDown() throws Exception{
            personRepository.deleteAll();
        }

        @Test
        public void test_step(){
                JobExecution jobExecution =
                        jobLauncherTestUtils.launchStep("stepPracticeTestProcessor");
                //launchStep도 jobExecution을 리턴하고 JobParameter를 넘길 수도 있음
            //Step에 붙은 Scope들이 제대로동작 하려면 SpringBatchTest 애노테이션이 있어야한다.

                Assertions.assertThat(jobExecution.getStepExecutions().stream()
                                .mapToInt(StepExecution::getWriteCount)//write한 값
                                .sum())
                        .isEqualTo(personRepository.count())
                        .isEqualTo(3);
        }

        @Test
        public void test_allow_duplicate() throws Exception {
            // given
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("allow_duplicate", "false")
                    .toJobParameters();

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

            // then
            System.out.println(jobExecution.getStepExecutions().stream().mapToInt(StepExecution::getWriteCount));
            Assertions.assertThat(jobExecution.getStepExecutions().stream()
                            .mapToInt(StepExecution::getWriteCount)//write한 값
                            .sum())
                    .isEqualTo(personRepository.count())
                    .isEqualTo(3);
        }
        @Test
        public void test_not_allow_duplicate() throws Exception {
            // given
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("allow_duplicate", "true")
                    .toJobParameters();

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

            // then
            System.out.println(jobExecution.getStepExecutions().stream().mapToInt(StepExecution::getWriteCount));
            Assertions.assertThat(jobExecution.getStepExecutions().stream()
                            .mapToInt(StepExecution::getWriteCount)//write한 값
                            .sum())
                    .isEqualTo(personRepository.count())
                    .isEqualTo(100);
        }

}
