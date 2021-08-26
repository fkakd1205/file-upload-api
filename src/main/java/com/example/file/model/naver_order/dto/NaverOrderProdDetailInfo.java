package com.example.file.model.naver_order.dto;

import lombok.Data;

@Data
public class NaverOrderProdDetailInfo {
    private String prodDetail1;     // 상품상세1 - 옵션정보(18) / 직접 수정해야 하는 항목
    private String optionManageCode;      // X - 옵션관리코드(13) / null 체크
    private int unit1;    // 내품수량1 - 수량(20)
}
