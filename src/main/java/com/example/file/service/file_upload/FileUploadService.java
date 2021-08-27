package com.example.file.service.file_upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Arrays;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.file.exception.FileUploadException;
import com.example.file.model.file_upload.FileUploadResponse;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileUploadService{
    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${file.upload-dir}")
    String fileLocation;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    // Image extension.
    private final List<String> EXTENSIONS_IMAGE = Arrays.asList("jpg", "gif", "png", "jpeg", "bmp", "tif");

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
        String newFileName = "PiaarMS_" + UUID.randomUUID().toString().replaceAll("-", "") + fileName;

        try {
            // fileLocation에 부분 경로 추가
            Path targetLocation = Paths.get(this.fileLocation + "\\" +newFileName);

            // targetLocation에 업로드 파일 복사
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }catch(Exception e) {
            throw new FileUploadException("["+fileName+"] File upload failed.",e);
        }

        return newFileName;
    }

    /**
     * Upload one api for image file to local.
     * <p>
     * 
     * @param file : MultipartFile
     * @return FileUploadResponse
     */
    public FileUploadResponse uploadFileToLocal(MultipartFile file) {

        String fileName = this.storeFile(file);
            
        String fileUploadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                    .path("/uploads/")
                                    .path(fileName)
                                    .toUriString();
            
        return new FileUploadResponse(fileName, fileUploadUri, file.getContentType(), file.getSize());
    }

    /**
     * Upload list api for image file to local.
     * <p>
     * 
     * @param file : List::MultipartFile::
     * @return List::FileUploadResponse::
     */
    public List<FileUploadResponse> uploadFilesToLocal(List<MultipartFile> files) {
        return files.stream()
                    .map(file -> uploadFileToLocal(file))
                    .collect(Collectors.toList());
    }

    /**
     * AWS S3 setting.
     */
    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    /**
     * Upload one api for image file to cloud.
     * <p>
     * 
     * @param file : MultipartFile
     * @return FileUploadResponse
     */
    public FileUploadResponse uploadFileToCloud(MultipartFile file) throws IOException {
        String fileName = "PiaarMS_" + UUID.randomUUID().toString().replaceAll("-", "") + file.getOriginalFilename();
        String uploadPath = bucket + "/uploads/image";

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(file.getSize());

        s3Client.putObject(new PutObjectRequest(uploadPath, fileName, file.getInputStream(), objMeta)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        
        return new FileUploadResponse(fileName, s3Client.getUrl(uploadPath, fileName).toString(), file.getContentType(), file.getSize());
    }

    /**
     * Upload list api for image file to cloud.
     * <p>
     * 
     * @param file : List::MultipartFile::
     * @return List::FileUploadResponse::
     */
    public List<FileUploadResponse> uploadFilesToCloud(List<MultipartFile> files) throws IOException {
        List<FileUploadResponse> uploadFiles = new ArrayList<>();

        for(MultipartFile file : files){
            uploadFiles.add(uploadFileToCloud(file));
        }

        return uploadFiles;
    }

    /**
     * File extension check.
     */
    public void isImageFile(List<MultipartFile> files) {
        for(MultipartFile file : files){
            String fileName = FilenameUtils.getExtension(file.getOriginalFilename().toLowerCase());

            if(EXTENSIONS_IMAGE.contains(fileName)){
                return;
            }
            throw new FileUploadException("This is not an image file.");
        }
    }

}
