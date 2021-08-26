package com.example.file.model.delivery_ready.repository;

import java.util.List;
import java.util.Set;

import com.example.file.model.delivery_ready.entity.DeliveryReadyItemEntity;
import com.example.file.model.delivery_ready.proj.DeliveryReadyItemViewProj;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryReadyItemRepository extends JpaRepository<DeliveryReadyItemEntity, Integer> {

    @Query(value = "SELECT prod_order_number FROM delivery_ready_item", nativeQuery = true)
    Set<String> findAllProdOrderNumber();

    @Query(value = "SELECT dri AS deliveryReadyItem, po.defaultName AS optionDefaultName, po.managementName AS optionManagementName, po.stockUnit AS optionStockUnit, p.managementName AS prodManagementName FROM DeliveryReadyItemEntity dri\n"
        + "LEFT JOIN ProductOptionEntity po ON dri.optionManagementCode = po.code\n"
        + "LEFT JOIN ProductEntity p ON po.productCid = p.cid\n"
        + "WHERE dri.released=:released")
    List<DeliveryReadyItemViewProj> findAllReleased(Boolean released);
}
