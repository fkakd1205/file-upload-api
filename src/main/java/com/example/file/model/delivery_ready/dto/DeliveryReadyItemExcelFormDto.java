package com.example.file.model.delivery_ready.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeliveryReadyItemExcelFormDto {
    private String prodOrderNumber;     // 상품주문번호
    private String orderNumber;      // 주문번호
    private String receiver;        // 수취인명
    private String receiverContact1;        // 수취인연락처1
    private String zipCode;        // 우편번호
    private String destination;        // 배송지
    private String transportNumber;     // 운송장번호
    private String prodName;        // 상품명
    private String sender;      // 보내는사람
    private String senderContact1;  // 전화번호1(지정)
    private String optionInfo;      // 상품상세1
    private String optionManagementCode;        // 옵션관리코드
    private Integer unit;        // 내품수량1
    private String deliveryMessage;     // 배송메시지
    private String unitA;       // 수량(A타입)
    private String allProdOrderNumber;      // 총 상품주문번호

    private boolean duplication;    // 받는사람 + 번호 + 주소 : 중복 여부
}
