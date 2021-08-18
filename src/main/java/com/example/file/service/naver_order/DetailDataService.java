package com.example.file.service.naver_order;

import java.util.List;
import java.util.UUID;

import com.example.file.model.naver_order.dto.DetailDataDto;
import com.example.file.model.naver_order.entity.DetailDataEntity;
import com.example.file.model.naver_order.repository.MemoryDetailDataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DetailDataService {
    
    @Autowired
    private MemoryDetailDataRepository memoryDetailDataRepository;

    public void createOne(DetailDataDto dto) {
        DetailDataEntity entity = new DetailDataEntity();
        
        entity.setCid(1).setId(UUID.randomUUID()).setFilePath(dto.getFileUploadUri()).setFileName(dto.getFileName())
                .setFileSize(dto.getSize()).setCreatedAt("2021-08-18").setCreatedBy("ce").setDeleted(0);

        memoryDetailDataRepository.save(entity);

        log.info("entity => {}", entity);
    }
}
