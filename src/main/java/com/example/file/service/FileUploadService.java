package com.example.file.service;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.example.file.exception.FileDownloadException;
import com.example.file.exception.FileUploadException;
import com.example.file.property.FileUploadProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService{

    private final Path fileLocation;
    
    @Autowired
    public FileUploadService(FileUploadProperties prop) {
        this.fileLocation = Paths.get(prop.getUploadDir()).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileLocation);
        }catch(Exception e) {
            throw new FileUploadException("Directory creation error.", e);
        }
    }

    /**
     * <b>File storage Method</b>
     * <p>
     * 설정 디렉토리에 파일을 저장한다.
     * 
     * @param file : MultipartFile
     * @return String
     */
    public String storeFile(MultipartFile file) {

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // fileLocation에 부분 경로 추가
            Path targetLocation = this.fileLocation.resolve(fileName);
            
            // targetLocation에 업로드 파일 복사
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }catch(Exception e) {
            throw new FileUploadException("["+fileName+"] File upload failed.",e);
        }

        return fileName;
    }

    /**
     * <b>File load Method</b>
     * <p>
     * 지정된 경로에서 파일을 가져온다.
     * 
     * @param fileName : String
     * @return UrlResource
     */
    public UrlResource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileLocation.resolve(fileName).normalize();
            UrlResource resource = new UrlResource(filePath.toUri());
            
            if(resource.exists()) {
                return resource;
            }else {
                throw new FileDownloadException("Not found fileName=" + fileName + ".");
            }
        }catch(MalformedURLException e) {
            throw new FileDownloadException("Not found fileName=" + fileName + ".");
        }
    }

}
