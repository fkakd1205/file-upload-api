package com.example.file.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.example.file.exception.FileDownloadException;
import com.example.file.payload.FileUploadResponse;
import com.example.file.service.FileUploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/file-upload")
public class FileUploadApiController {
    
    @Autowired
    private FileUploadService fileUploadservice;
    
    /**
     * Upload one api for image file.
     * <p>
     * <b>POST : API URL => /api/v1/file-upload/uploadFile</b>
     * 
     * @param file : MultipartFile
     * @return FileUploadResponse
     */
    @PostMapping("/uploadFile")
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileUploadservice.storeFile(file);

        log.info("file => {}", file);
        
        String fileUploadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/v1/file-upload/images/")
                                .path(fileName)
                                .toUriString();
        
        return new FileUploadResponse(fileName, fileUploadUri, file.getContentType(), file.getSize());
    }
    
    /**
     * Upload list api for image file.
     * <p>
     * <b>POST : API URL => /api/v1/file-upload/uploadFiles</b>
     * 
     * @param files : List::MultipartFile::
     * @return List::FileUploadResponse::
     */
    @PostMapping("/uploadFiles")
    public List<FileUploadResponse> uploadFiles(@RequestParam("files") List<MultipartFile> files){
        return files.stream()
                    .map(file -> uploadFile(file))
                    .collect(Collectors.toList());
    }

    /**
     * Select one api for image file.
     * <p>
     * <b>POST : API URL => /api/v1/file-upload/images/{fileName}</b>
     * 
     * @param fileName : String
     * @param request : HttpServletRequest
     * @return ResponseEntity::UrlResource::
     */
    @GetMapping("/images/{fileName}")
    public ResponseEntity<UrlResource> getUploadFile(@PathVariable String fileName, HttpServletRequest request){

        UrlResource resource = fileUploadservice.loadFileAsResource(fileName);
 
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new FileDownloadException("Could not determine file type.");
        }
 
        if(contentType == null) {
            contentType = "application/octet-stream";
        }
 
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
