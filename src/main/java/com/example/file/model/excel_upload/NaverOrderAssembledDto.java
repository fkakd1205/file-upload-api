package com.example.file.model.excel_upload;

import java.util.List;

import lombok.Data;

@Data
public class NaverOrderAssembledDto{
    private String orderNumber;     // X - 주문번호(1)
    private String receiver;    // 받는사람 - 수취인명(10)
    private String receiverContact1;    // 전화번호1 - 수취인연락처1(40)
    private String zipCode;    // 우편번호 - 우편번호(44)
    private String destination;    // 주소 - 배송지(42)
    private String tranportNumber;    // 운송장번호 - X
    private String prodName1;    // 상품명1 - 상품명(16)
    private String sender;    // 보내는사람(지정) - X
    private String senderContact1;    // 전화번호1(지정) - X
    private List<NaverOrderProdDetailInfo> prodDetailInfos;    // 상품상세1, 내품수량1
    private String deliveryMessage;    // 배송메시지 - 배송메세지(45)
    private String unitA;    // 수량(A타입) - X
    private String prodOrderNumber;     // 상품주문번호 - 상품주문번호(0)
}
