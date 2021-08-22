package com.example.file.model.naver_order.entity;

import java.util.Date;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExcelDataEntity {
    private String prodOrderNumber;     // 상품주문번호(0)
    private String orderNumber;      // 주문번호(1)
    private String salesChannel;        // 판매채널(7)
    private String buyer;       // 구매자명(8)
    private String buyerId;     // 구매자ID(9)
    private String receiver;        // 수취인명(10)
    private Date paymentDate;     // 결제일(14)
    private String prodNumber;        // 상품번호(15)
    private String prodName;        // 상품명(16)
    private String optionInfo;        // 옵션정보(18)
    private String optionManageCode;        // 옵션관리코드(19) / null 체크
    private Integer unit;        // 수량(20)
    private Date orderConfirmationDate;        // 발주확인일(27)
    private Date shipmentDueDate;        // 발송기한(28)
    private String shipmentCostBundleNumber;        // 배송비 묶음번호(32)
    private String sellerProdCode;        // 판매자 상품코드(37) / null 체크
    private String sellerInnerCode1;        // 판매자 내부코드1(38) / null 체크
    private String sellerInnerCode2;        // 판매자 내부코드2(39) / null 체크 
    private String receiverContact1;        // 수취인연락처1(40)
    private String receiverContact2;        // 수취인연락처2(41) / null 체크
    private String destination;        // 배송지(42)
    private String buyerContact;        // 구매자연락처(43)
    private String zipCode;        // 우편번호(44)
    private String deliveryMessage;        // 배송메세지(45) / null 체크
    private String releaseArea;        // 출고지(46)
    private Date orderDateTime;        // 주문일시(56)
}
