package com.example.file.model.naver_order.entity;

import java.util.Date;
import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DetailDataEntity {
    private Integer cid;
    private UUID id;     // UUID로 변경해야 함
    private String filePath;
    private String fileName;
    private Long fileSize;
    private Date createdAt;
    private UUID createdBy;   // UUID로 변경해야 함
    private Integer deleted;
}
