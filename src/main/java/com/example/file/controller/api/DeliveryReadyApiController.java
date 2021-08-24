package com.example.file.controller.api;

import java.io.IOException;

import com.example.file.model.message.Message;
import com.example.file.service.delivery_ready.DeliveryReadyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/delivery-ready")
public class DeliveryReadyApiController {
    
    @Autowired
    private DeliveryReadyService deliveryReadyService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDeliveryReadyExcelFile(@RequestParam("file") MultipartFile file) throws IOException {
        Message message = new Message();

        // file extension check.
        try{
            deliveryReadyService.isExcelFile(file);
        } catch(Exception e){
            message.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            message.setMessage("file_extension_error");
            message.setMemo("This is not an excel file.");
            return new ResponseEntity<>(message, message.getStatus());
        }

        try{
            message.setData(deliveryReadyService.uploadDeliveryReadyExcelFile(file));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    // 엑셀 파일 AWS S3업로드 후 DeliveryReadyFileEntity, DeliveryReadyItemEntity 생성
    @PostMapping("/store")
    public ResponseEntity<?> storeDeliveryReadyExcelFile(@RequestParam("file") MultipartFile file) throws IOException {
        Message message = new Message();

        try{
            message.setData(deliveryReadyService.storeDeliveryReadyExcelFile(file));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/view/unreleased")
    public ResponseEntity<?> getDeliveryReadyViewUnreleasedData() {
        Message message = new Message();

        try{
            message.setData(deliveryReadyService.getDeliveryReadyViewUnreleasedData());
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/view/released")
    public ResponseEntity<?> getDeliveryReadyViewReleasedData() {
        Message message = new Message();

        try{
            message.setData(deliveryReadyService.getDeliveryReadyViewReleasedData());
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }
}
