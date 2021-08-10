package com.example.file.model.excel_upload;

import lombok.Data;

@Data
public class ExcelUploadGetDto {
    private String buyer;   // 구매자명
    private String productName;    // 상품명
    private String productCode;    // 상품코드
    private String destination;    // 배송지
    private String buyerContact;    // 구매자 연락처
    private String deliveryMessage;    // 배송 메세지
}
