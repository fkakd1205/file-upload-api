package com.example.file.service.naver_order;

import java.util.List;

import com.example.file.model.naver_order.entity.ExcelDataEntity;
import com.example.file.model.naver_order.repository.MemoryExcelDataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExcelDataService {
    @Autowired
    private MemoryExcelDataRepository excelDataRepository;

    int cid = 0;

    public void createExcelData(List<ExcelDataEntity> entities) {

        for(ExcelDataEntity entity : entities) {
            ExcelDataEntity newEntity = new ExcelDataEntity();

            newEntity.setProdOrderNumber(entity.getProdOrderNumber())
                .setOrderNumber(entity.getOrderNumber())
                .setSalesChannel(entity.getSalesChannel())
                .setBuyer(entity.getBuyer())
                .setBuyerId(entity.getBuyerId())
                .setReceiver(entity.getReceiver())
                .setPaymentDate(entity.getPaymentDate())
                .setProdNumber(entity.getProdNumber())
                .setProdName(entity.getProdName())
                .setOptionInfo(entity.getOptionInfo())
                .setOptionManageCode(entity.getOptionManageCode())
                .setUnit((int) entity.getUnit())
                .setOrderConfirmationDate(entity.getOrderConfirmationDate())
                .setShipmentDueDate(entity.getShipmentDueDate())
                .setShipmentCostBundleNumber(entity.getShipmentCostBundleNumber())
                .setSellerProdCode(entity.getSellerProdCode())
                .setSellerInnerCode1(entity.getSellerInnerCode1())
                .setSellerInnerCode2(entity.getSellerInnerCode2())
                .setReceiverContact1(entity.getReceiverContact1())
                .setReceiverContact2(entity.getReceiverContact2())
                .setDestination(entity.getDestination())
                .setBuyerContact(entity.getBuyerContact())
                .setZipCode(entity.getZipCode())
                .setDeliveryMessage(entity.getDeliveryMessage())
                .setReleaseArea(entity.getReleaseArea())
                .setOrderDateTime(entity.getOrderDateTime());

            excelDataRepository.save(entity);
        }
    }
}
