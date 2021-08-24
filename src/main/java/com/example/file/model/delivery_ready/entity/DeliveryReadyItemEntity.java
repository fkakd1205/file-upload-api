package com.example.file.model.delivery_ready.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Table(name = "delivery_ready_item")
@Accessors(chain=true)
public class DeliveryReadyItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid")
    private Integer cid;

    @Type(type = "uuid-char")
    @Column(name = "id")
    private UUID id;

    @Column(name = "prod_order_number")
    private String prodOrderNumber;     // 상품주문번호(0)

    @Column(name = "order_number")
    private String orderNumber;      // 주문번호(1)

    @Column(name = "sales_channel")
    private String salesChannel;        // 판매채널(7)

    @Column(name = "buyer")
    private String buyer;       // 구매자명(8)

    @Column(name = "buyer_id")
    private String buyerId;     // 구매자ID(9)

    @Column(name = "receiver")
    private String receiver;        // 수취인명(10)

    @Column(name = "payment_date")
    private Date paymentDate;     // 결제일(14)

    @Column(name = "prod_number")
    private String prodNumber;        // 상품번호(15)

    @Column(name = "prod_name")
    private String prodName;        // 상품명(16)

    @Column(name = "option_info")
    private String optionInfo;        // 옵션정보(18)

    @Column(name = "option_management_code")
    private String optionManagementCode;        // 옵션관리코드(19) / null 체크

    @Column(name = "unit")
    private Integer unit;        // 수량(20)

    @Column(name = "order_confirmation_date")
    private Date orderConfirmationDate;        // 발주확인일(27)

    @Column(name = "shipment_due_date")
    private Date shipmentDueDate;        // 발송기한(28)

    @Column(name = "shipment_cost_bundle_number")
    private String shipmentCostBundleNumber;        // 배송비 묶음번호(32)

    @Column(name = "seller_prod_code")
    private String sellerProdCode;        // 판매자 상품코드(37) / null 체크

    @Column(name = "seller_inner_code1")
    private String sellerInnerCode1;        // 판매자 내부코드1(38) / null 체크

    @Column(name = "seller_inner_code2")
    private String sellerInnerCode2;        // 판매자 내부코드2(39) / null 체크
    
    @Column(name = "receiver_contact1")
    private String receiverContact1;        // 수취인연락처1(40)

    @Column(name = "receiver_contact2")
    private String receiverContact2;        // 수취인연락처2(41) / null 체크

    @Column(name = "destination")
    private String destination;        // 배송지(42)

    @Column(name = "buyer_contact")
    private String buyerContact;        // 구매자연락처(43)

    @Column(name = "zip_code")
    private String zipCode;        // 우편번호(44)

    @Column(name = "delivery_message")
    private String deliveryMessage;        // 배송메세지(45) / null 체크

    @Column(name = "release_area")
    private String releaseArea;        // 출고지(46)

    @Column(name = "order_date_time")
    private Date orderDateTime;        // 주문일시(56)

    @Column(name = "released")
    private Boolean released;   // 출고여부

    @Column(name = "released_at")
    private Date releasedAt;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "delivery_ready_file_cid")
    private Integer deliveryReadyFileCid;
}
