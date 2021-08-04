package com.example.file.model.message;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class Message {
    private HttpStatus status;
    private String message;
    private String memo;
    private Object data;
    private Object pagenation;

    public Message() {
        this.status = HttpStatus.BAD_REQUEST;
        this.message = null;
        this.memo = null;
        this.data = null;
        this.pagenation = null;
    }

    public int getStatusCode(){
        return this.status.value();
    }
}
