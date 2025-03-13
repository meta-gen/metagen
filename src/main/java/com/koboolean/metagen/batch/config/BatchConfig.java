package com.koboolean.metagen.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
public class BatchConfig {

    Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    @Bean
    public Job batchJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("batchJob",jobRepository)
                .start(batchJobStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step batchJobStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("batchStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Batch job started");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }
}
