package com.example.file.model.excel_upload;

import lombok.Data;

@Data
public class NaverOrderProdDetailInfo {
    private String prodDetail1;     // 상품상세1 - 옵션정보(18) / 직접 수정해야 하는 항목
    private int unit1;    // 내품수량1 - 수량(20)
}
