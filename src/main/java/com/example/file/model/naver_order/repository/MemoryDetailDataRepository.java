package com.example.file.model.naver_order.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.file.model.naver_order.entity.DetailDataEntity;

import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class MemoryDetailDataRepository {
    private Map<Integer, DetailDataEntity> memoryDetailData = new HashMap<>();
    private Integer seq = 0;

    public List<DetailDataEntity> findAll() {
        List<DetailDataEntity> memoryDetailDataList = new ArrayList<>();

        for(int i = 0; i < memoryDetailData.size(); i++){
            memoryDetailDataList.add(memoryDetailData.get(i));
        }
        return memoryDetailDataList;
    }

    public DetailDataEntity save(DetailDataEntity entity) {
        memoryDetailData.put(seq, entity);
        seq++;

        log.info("DetailDataRepository entity => {}", entity);
        return entity;
    }

    public void clearAll() {
        memoryDetailData.clear();
    }
}
