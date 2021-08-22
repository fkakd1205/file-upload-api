package com.example.file.model.naver_order.entity;

import java.util.Date;
import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DetailDataEntity {
    private Integer cid;
    private UUID id; 
    private String filePath;
    private String fileName;
    private Long fileSize;
    private Date createdAt;
    private UUID createdBy;
    private Integer deleted;
}
