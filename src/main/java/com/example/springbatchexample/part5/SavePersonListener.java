package com.example.springbatchexample.part5;


import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.annotation.BeforeStep;

@Slf4j
public class SavePersonListener {//편의상 listener을 내부 클래스로 만듬

    //0.이 Listener들을 JOB에서 등록을 해줘야한다.
    //1.interface 방식
    public static class SavePersonJobExecutionListener implements JobExecutionListener{    //JobExecutionListener만들려면 구현해야함

            @Override
            public void beforeJob(JobExecution jobExecution) {  //잡 실행전에 실행되는 메소드
                        log.info("beforeJob");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {   //잡 실행 후에 실행되는 메소드
                       int sum = jobExecution
                               .getStepExecutions()
                               .stream()
                               .mapToInt(StepExecution::getWriteCount).
                               sum();

                        log.info("afterJOb : {} ",sum);
            }

    }
    //2.annotation 방식
    public static class SavePersonAnnotationJobExecution{
            @BeforeJob
            public void before(JobExecution jobExecution){  //메소드명은 상관없음 매개변수와 애노테이션만 있으면 된다
                log.info("Annotation BeforeJob");
            }

            @AfterJob
            public void afterJob(JobExecution jobExecution) {
                int sum = jobExecution
                        .getStepExecutions()
                        .stream()
                        .mapToInt(StepExecution::getWriteCount).
                        sum();

                log.info("afterJOb : {} ",sum);
            }
    }

    public static class SavePersonStepExecutionListener{
            @BeforeStep
            public void beforeStep(StepExecution stepExecution){
                log.info("before Step");
            }

            @AfterStep
            public ExitStatus afterStep(StepExecution stepExecution){
                log.info("after Step : {}",stepExecution.getWriteCount());
                /*이런식으로 상태변환가능*/
//                if(stepExecution.getWriteCount() == 0){
//                    return ExitStatus.FAILED;
//                }

                return stepExecution.getExitStatus();   //배치는 종료되거나 에러나면 상태를 stepExecution에 저장한다

            }
    }


}
