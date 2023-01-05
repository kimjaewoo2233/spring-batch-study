package com.example.springbatchexample.part6;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Component
@RequiredArgsConstructor
public class SaveUserTasklet implements Tasklet {

    private final UserRepository userRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        List<User> userList = createUsers();

        Collections.shuffle(userList);

        userRepository.saveAll(userList);
        return RepeatStatus.FINISHED;
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {//100.for
            users.add(User.builder()
                            .level(Level.NORMAL)
                            .totalAmount(1_000)
                            .username("test username"+i)
                    .build());
        }// 100개에 유저가 전부 1천원을 가지고 있음

        for (int i = 100; i < 200; i++) {
            users.add(User.builder()
                    .level(Level.NORMAL)
                    .totalAmount(200_000)
                    .username("test username"+i)
                    .build());
        }//20만원을 가진 회원이지만 기본 값이 normal임

        for (int i = 200; i < 300; i++) {
            users.add(User.builder()
                    .level(Level.NORMAL)
                    .totalAmount(300_000)
                    .username("test username"+i)
                    .build());
        }//30만운을 가진 회원이지만 현재 기본 값이 NORMAL

        for (int i = 300; i < 400; i++) {
            users.add(User.builder()
                    .level(Level.NORMAL)
                    .totalAmount(500_000)
                    .username("test username"+i)
                    .build());
        }

        return users;
    }
}
