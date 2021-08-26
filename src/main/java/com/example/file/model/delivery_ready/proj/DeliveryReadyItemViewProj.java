package com.example.file.model.delivery_ready.proj;

import com.example.file.model.delivery_ready.entity.DeliveryReadyItemEntity;

public interface DeliveryReadyItemViewProj {
    DeliveryReadyItemEntity getDeliveryReadyItem();
    String getOptionDefaultName();
    String getOptionManagementName();
    Integer getOptionStockUnit();
    String getProdManagementName();
}
