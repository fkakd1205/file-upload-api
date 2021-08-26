package com.example.file.model.delivery_ready.dto;

import com.example.file.model.delivery_ready.entity.DeliveryReadyItemEntity;

import lombok.Data;

@Data
public class DeliveryReadyItemViewDto {
    private DeliveryReadyItemEntity deliveryReadyItem;
    private String optionDefaultName;
    private String optionManagementName;
    private String optionStockUnit;
    private String prodManagementName;
}
