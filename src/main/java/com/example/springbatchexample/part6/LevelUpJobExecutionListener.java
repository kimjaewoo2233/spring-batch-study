package com.example.springbatchexample.part6;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class LevelUpJobExecutionListener implements JobExecutionListener {

    private final UserRepository userRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        //JobExecution에는 시작시간과 종료시간이 있음 종료시간 - 시작시간 배치가 실행한시간이 나옴
        Collection<User> users =
                    userRepository.findAllByUpdatedDate(LocalDate.now());
            //위에 코드를 통해 업데이트된 놈들을 가져옴
            long time = jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime();
            log.info("회원등급 업데이트 배치 프로그램");
            log.info("------------------------------");
            log.info("총 데이터 처리 {}건, 처리 시간 {}millis",users.size(),time);

    }
}
