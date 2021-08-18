package com.example.file.model.naver_order.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.file.model.naver_order.entity.ExcelDataEntity;

public class MemoryExcelDataRepository {
    private Map<Integer, ExcelDataEntity> memoryExcelData = new HashMap<>();
    private Integer seq = 0;

    public List<ExcelDataEntity> findAll() {
        List<ExcelDataEntity> memoryExcelDataList = new ArrayList<>();

        for(int i = 0; i < memoryExcelData.size(); i++){
            memoryExcelDataList.add(memoryExcelData.get(i));
        }
        return memoryExcelDataList;
    }

    public ExcelDataEntity save(ExcelDataEntity entity) {
        memoryExcelData.put(seq, entity);
        seq++;
        
        return entity;
    }

    public void clearAll() {
        memoryExcelData.clear();
    }
}
