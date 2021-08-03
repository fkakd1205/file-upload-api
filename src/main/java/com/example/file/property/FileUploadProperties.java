package com.example.file.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

// application.properties의 file설정과 연결
@ConfigurationProperties(prefix = "file")
public class FileUploadProperties {
    private String uploadDir;
 
    public String getUploadDir() {
        return uploadDir;
    }
 
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
