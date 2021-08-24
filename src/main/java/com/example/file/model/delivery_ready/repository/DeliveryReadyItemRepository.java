package com.example.file.model.delivery_ready.repository;

import java.util.List;
import java.util.Set;

import com.example.file.model.delivery_ready.dto.DeliveryReadyItemViewDto;
import com.example.file.model.delivery_ready.entity.DeliveryReadyItemEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryReadyItemRepository extends JpaRepository<DeliveryReadyItemEntity, Integer> {

    @Query(value = "SELECT prod_order_number FROM delivery_ready_item", nativeQuery = true)
    Set<String> findAllProdOrderNumber();

    // List<DeliveryReadyItemEntity> findByReleased(Boolean released);

    @Query(value = "SELECT dri.*, po.default_name, po.management_name, po.stock_unit, p.management_name product_management_name FROM piaar_dev1.delivery_ready_item dri\n"
        + "LEFT JOIN piaar_dev1.product_option po ON dri.option_management_code = po.code\n"
        + "LEFT JOIN piaar_dev1.product p ON po.product_cid = p.cid\n"
        + "WHERE dri.released=:released", nativeQuery = true)
    List<DeliveryReadyItemViewDto> findAllReleased(Boolean released);

}
