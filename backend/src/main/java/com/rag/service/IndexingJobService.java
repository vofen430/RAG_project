package com.rag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rag.entity.IndexingJobEntity;
import com.rag.mapper.IndexingJobMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class IndexingJobService {

    private final IndexingJobMapper indexingJobMapper;

    public IndexingJobService(IndexingJobMapper indexingJobMapper) {
        this.indexingJobMapper = indexingJobMapper;
    }

    public IndexingJobEntity createJob(String documentId) {
        IndexingJobEntity job = new IndexingJobEntity();
        job.setId(UUID.randomUUID().toString());
        job.setDocumentId(documentId);
        job.setJobStatus("RUNNING");
        job.setCurrentStage("INIT");
        job.setProcessedChunks(0);
        job.setTotalChunks(0);
        job.setStartedAt(OffsetDateTime.now());
        indexingJobMapper.insert(job);
        return job;
    }

    public void updateJob(IndexingJobEntity job) {
        indexingJobMapper.updateById(job);
    }

    public IndexingJobEntity getLatestJob(String documentId) {
        return indexingJobMapper.selectOne(
                new LambdaQueryWrapper<IndexingJobEntity>()
                        .eq(IndexingJobEntity::getDocumentId, documentId)
                        .orderByDesc(IndexingJobEntity::getCreatedAt)
                        .last("LIMIT 1")
        );
    }

    public void markCompleted(IndexingJobEntity job, int totalChunks) {
        job.setJobStatus("COMPLETED");
        job.setCurrentStage("DONE");
        job.setTotalChunks(totalChunks);
        job.setProcessedChunks(totalChunks);
        job.setFinishedAt(OffsetDateTime.now());
        indexingJobMapper.updateById(job);
    }

    public void markFailed(IndexingJobEntity job, String errorMessage) {
        job.setJobStatus("FAILED");
        job.setErrorMessage(errorMessage);
        job.setFinishedAt(OffsetDateTime.now());
        indexingJobMapper.updateById(job);
    }
}
