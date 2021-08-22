package com.example.file.service.naver_order;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.example.file.model.naver_order.entity.DetailDataEntity;
import com.example.file.model.naver_order.repository.MemoryDetailDataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DetailDataService {
    
    @Autowired
    private MemoryDetailDataRepository memoryDetailDataRepository;

    int cid = 0;

    public void createDetailData(String filePath, String fileName, Long fileSize) {
        DetailDataEntity entity = new DetailDataEntity();
        Date date = Calendar.getInstance().getTime();

        entity.setCid(cid++).setId(UUID.randomUUID()).setFilePath(filePath).setFileName(fileName)
                .setFileSize(fileSize).setCreatedAt(date).setCreatedBy(UUID.randomUUID()).setDeleted(0);

        memoryDetailDataRepository.save(entity);
    }
}
