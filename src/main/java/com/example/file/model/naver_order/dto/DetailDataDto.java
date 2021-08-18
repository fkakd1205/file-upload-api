package com.example.file.model.naver_order.dto;

import lombok.Data;

@Data
public class DetailDataDto {
    private String fileUploadUri;
    private String fileName;
    private Long size;
}
