package com.example.springbatchexample.part5;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;


@Slf4j
public class ChunkPracListener implements ChunkListener {
    @Override
    public void beforeChunk(ChunkContext context) {
        log.info("chunk 시작");
                log.info(context.getStepContext().getStepExecution().getStepName());
    }

    @Override
    public void afterChunk(ChunkContext context) {
        log.info("chunk 끝");
    }

    @Override
    public void afterChunkError(ChunkContext context) {

    }
}
