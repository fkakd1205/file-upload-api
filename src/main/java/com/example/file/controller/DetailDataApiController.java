package com.example.file.controller;

import com.example.file.model.message.Message;
import com.example.file.model.naver_order.entity.DetailDataEntity;
import com.example.file.service.naver_order.DetailDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/detail-data")
public class DetailDataApiController {
    
    @Autowired
    private DetailDataService detailDataService;

    // @GetMapping("/one/{productCid}")
    // public ResponseEntity<?> searchOne(@PathVariable(value = "productCid") Integer productCid) {
    //     Message message = new Message();

    //     if (!userService.isUserLogin()) {
    //         message.setStatus(HttpStatus.FORBIDDEN);
    //         message.setMessage("need_login");
    //         message.setMemo("need login");
    //     } else{
    //         try {
    //             message.setData(productService.searchOne(productCid));
    //             message.setStatus(HttpStatus.OK);
    //             message.setMessage("success");
    //         } catch (NullPointerException e) {
    //             message.setStatus(HttpStatus.NOT_FOUND);
    //             message.setMessage("Not found productCid=" + productCid + " value.");
    //         }
    //     }

    //     return new ResponseEntity<>(message, message.getStatus());
    // }

    // @PostMapping("/one")
    // public ResponseEntity<?> createOne(@RequestBody DetailDataEntity detailDataDto) {
    //     Message message = new Message();

    //     // // 유저 권한을 체크한다.
    //     // if (detailDataService.isManager()) {
    //         try{
    //             detailDataService.createOne(detailDataDto);
    //             message.setStatus(HttpStatus.OK);
    //             message.setMessage("success");
    //         } catch(Exception e) {
    //             message.setStatus(HttpStatus.BAD_REQUEST);
    //             message.setMessage("error");
    //         }
    //     // } else {
    //     //     userService.userDenyCheck(message);
    //     // }

    //     return new ResponseEntity<>(message, message.getStatus());
    // }
}
