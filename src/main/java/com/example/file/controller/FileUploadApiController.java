package com.example.file.controller;

import java.util.List;

import com.example.file.model.message.Message;
import com.example.file.service.file_upload.FileUploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/file-upload")
public class FileUploadApiController {
    
    @Autowired
    private FileUploadService fileUploadservice;

    /**
     * Upload files api for image files to local.
     * <p>
     * <b>GET : API URL => /api/v1/file-upload/uploadFiles</b>
     * 
     * @param files : List::MultipartFile::
     * @return ResponseEntity(message, HttpStatus)
     * @see FileUploadService#uploadFiles
     * @see UserService#userDenyCheck
     */
    @PostMapping("/uploadFilesToLocal")
    public ResponseEntity<?> uploadFilesToLocal(@RequestParam("files") List<MultipartFile> files) {
        Message message = new Message();
        
        // file extension check.
        try{
            fileUploadservice.isImageFile(files);
        } catch(Exception e){
            message.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            message.setMessage("file_extension_error");
            message.setMemo("This is not an image file.");
            return new ResponseEntity<>(message, message.getStatus());
        }

        try{
            message.setData(fileUploadservice.uploadFilesToLocal(files));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    /**
     * Upload files api for image files to cloud.
     * <p>
     * <b>GET : API URL => /api/v1/file-upload/uploadFiles</b>
     * 
     * @param files : List::MultipartFile::
     * @return ResponseEntity(message, HttpStatus)
     * @see FileUploadService#uploadFiles
     * @see UserService#userDenyCheck
     */
    @PostMapping("/uploadFilesToCloud")
    public ResponseEntity<?> uploadFilesToCloud(@RequestParam("files") List<MultipartFile> files) {
        Message message = new Message();

        // file extension check.
        try{
            fileUploadservice.isImageFile(files);
        } catch(Exception e){
            message.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            message.setMessage("file_extension_error");
            message.setMemo("This is not an image file.");
            return new ResponseEntity<>(message, message.getStatus());
        }

        try{
            message.setData(fileUploadservice.uploadFilesToCloud(files));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }
}
