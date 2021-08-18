package com.example.file.model.naver_order.entity;

import lombok.Data;

@Data
public class ExcelDataEntity {
    private String prodOrderNumber;     // 상품주문번호
    private String oderNumber;      // 주문번호
    private String salesChannel;        // 판매채널
    private String buyer;       // 구매자명
    private String buyerId;     // 구매자ID
    private String receiver;        // 수취인명
    private String paymentDate;     // 결제일
    private String prodNumber;        // 상품번호
    private String prodName;        // 상품명
    private String optionInfo;        // 옵션정보
    private String optionManageCode;        // 옵션관리코드
    private Integer unit;        // 수량
    private String orderConfirmationDate;        // 발주확인일
    private String shipmentDueDate;        // 발송기한
    private String shipmentCostBundleNumber;        // 배송비 묶음번호
    private String sellerProdCode;        // 판매자 상품코드
    private String sellerInnerCode1;        // 판매자 내부코드1
    private String sellerInnerCode2;        // 판매자 내부코드2
    private String receiverContact1;        // 수취인연락처1
    private String receiverContact2;        // 수취인연락처2
    private String destination;        // 배송지
    private String buyerContact;        // 구매자연락처
    private String zipCode;        // 우편번호
    private String deliveryMessage;        // 배송메세지
    private String releaseArea;        // 출고지
    private String orderDateAndTime;        // 주문일시
}
