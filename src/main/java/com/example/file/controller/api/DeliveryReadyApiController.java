package com.example.file.controller.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import com.example.file.model.delivery_ready.dto.DeliveryReadyItemExcelFormDto;
import com.example.file.model.delivery_ready.dto.DeliveryReadyItemViewDto;
import com.example.file.model.message.Message;
import com.example.file.service.delivery_ready.DeliveryReadyService;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/delivery-ready")
public class DeliveryReadyApiController {
    
    @Autowired
    private DeliveryReadyService deliveryReadyService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDeliveryReadyExcelFile(@RequestParam("file") MultipartFile file) throws IOException {
        Message message = new Message();

        // file extension check.
        try{
            deliveryReadyService.isExcelFile(file);
        } catch(Exception e){
            message.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            message.setMessage("file_extension_error");
            message.setMemo("This is not an excel file.");
            return new ResponseEntity<>(message, message.getStatus());
        }

        try{
            message.setData(deliveryReadyService.uploadDeliveryReadyExcelFile(file));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    // 엑셀 파일 AWS S3업로드 후 DeliveryReadyFileEntity, DeliveryReadyItemEntity 생성
    @PostMapping("/store")
    public ResponseEntity<?> storeDeliveryReadyExcelFile(@RequestParam("file") MultipartFile file) throws IOException {
        Message message = new Message();

        try{
            message.setData(deliveryReadyService.storeDeliveryReadyExcelFile(file));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/view/unreleased")
    public ResponseEntity<?> getDeliveryReadyViewUnreleasedData() {
        Message message = new Message();

        try{
            message.setData(deliveryReadyService.getDeliveryReadyViewUnreleasedData());
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/view/released/{currentDate}")
    public ResponseEntity<?> getDeliveryReadyViewReleasedData(@PathVariable(value = "currentDate") String currentDate) {
        Message message = new Message();

        try{
            message.setData(deliveryReadyService.getDeliveryReadyViewReleasedData(currentDate));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/view/release/{date1}&&{date2}")
    public ResponseEntity<?> getDeliveryReadyViewReleased(@PathVariable(value = "date1") String date1, @PathVariable(value="date2") String date2) {
        Message message = new Message();

        try{
            message.setData(deliveryReadyService.getDeliveryReadyViewReleased(date1, date2));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/view/deleteOne/{itemId}")
    public ResponseEntity<?> deleteOneDeliveryReadyViewData(@PathVariable(value = "itemId") UUID itemId) {
        Message message = new Message();

        try{
            deliveryReadyService.deleteOneDeliveryReadyViewData(itemId);
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/view/updateOne/{itemId}")
    public ResponseEntity<?> updateReleasedDeliveryReadyItem(@PathVariable(value = "itemId") UUID itemId) {
        Message message = new Message();

        try{
            deliveryReadyService.updateReleasedDeliveryReadyItem(itemId);
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/view/searchList/productInfo")
    public ResponseEntity<?> searchDeliveryReadyItemProductInfo() {
        Message message = new Message();

        try{
            deliveryReadyService.searchDeliveryReadyItemProductInfo();
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    // @GetMapping("/view/updateOptionCode/{itemId}")
    // public ResponseEntity<?> updateOptionManagementCode(@PathVariable(value = "itemId") UUID itemId) {
    //     Message message = new Message();

    //     try{
    //         deliveryReadyService.updateOptionManagementCode(itemId);
    //         message.setStatus(HttpStatus.OK);
    //         message.setMessage("success");
    //     } catch(Exception e) {
    //         message.setStatus(HttpStatus.BAD_REQUEST);
    //         message.setMessage("error");
    //     }

    //     return new ResponseEntity<>(message, message.getStatus());
    // }

    @PostMapping("/view/download")
    public void downloadExcelFile(HttpServletResponse response, @RequestBody List<DeliveryReadyItemViewDto> viewDtos) {

        // 중복데이터 처리
        List<DeliveryReadyItemExcelFormDto> dtos = deliveryReadyService.changeDeliveryReadyItem(viewDtos);

        // 엑셀 생성
        Workbook workbook = new XSSFWorkbook();     // .xlsx
        Sheet sheet = workbook.createSheet("발주서 양식");
        Row row = null;
        Cell cell = null;
        int rowNum = 0;

        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("주문번호");
        cell = row.createCell(1);
        cell.setCellValue("상품주문번호");
        cell = row.createCell(2);
        cell.setCellValue("받는사람");
        cell = row.createCell(3);
        cell.setCellValue("전화번호1");
        cell = row.createCell(4);
        cell.setCellValue("우편번호");
        cell = row.createCell(5);
        cell.setCellValue("주소");
        cell = row.createCell(6);
        cell.setCellValue("운송장번호");
        cell = row.createCell(7);
        cell.setCellValue("상품명1");
        cell = row.createCell(8);
        cell.setCellValue("보내는사람(지정)");
        cell = row.createCell(9);
        cell.setCellValue("전화번호1(지정)");
        cell = row.createCell(10);
        cell.setCellValue("옵션관리코드");
        cell = row.createCell(11);
        cell.setCellValue("내품수량1");
        cell = row.createCell(12);
        cell.setCellValue("배송메시지");
        cell = row.createCell(13);
        cell.setCellValue("수량(A타입)");
        cell = row.createCell(14);
        cell.setCellValue("총 상품주문번호");
        cell = row.createCell(15);
        cell.setCellValue("상품상세1");

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        cellStyle.setFillPattern(FillPatternType.BRICKS);

        for (int i=0; i<dtos.size(); i++) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(dtos.get(i).getOrderNumber());
            cell = row.createCell(1);
            cell.setCellValue(dtos.get(i).getProdOrderNumber());
            cell = row.createCell(2);
            // 받는사람 + 번호 + 주소 : 중복데이터 엑셀 셀 색상 설정
            if(dtos.get(i).isDuplication()){
                cell.setCellStyle(cellStyle);
            }
            cell.setCellValue(dtos.get(i).getReceiver());
            cell = row.createCell(3);
            cell.setCellValue(dtos.get(i).getReceiverContact1());
            cell = row.createCell(4);
            cell.setCellValue(dtos.get(i).getZipCode());
            cell = row.createCell(5);
            cell.setCellValue(dtos.get(i).getDestination());
            cell = row.createCell(6);
            cell.setCellValue(dtos.get(i).getTransportNumber());
            cell = row.createCell(7);
            cell.setCellValue(dtos.get(i).getProdName());
            cell = row.createCell(8);
            cell.setCellValue(dtos.get(i).getSender());
            cell = row.createCell(9);
            cell.setCellValue(dtos.get(i).getSenderContact1());
            cell = row.createCell(10);
            cell.setCellValue(dtos.get(i).getOptionManagementCode());
            cell = row.createCell(11);
            cell.setCellValue(dtos.get(i).getUnit());
            cell = row.createCell(12);
            cell.setCellValue(dtos.get(i).getDeliveryMessage());
            cell = row.createCell(13);
            cell.setCellValue(dtos.get(i).getUnitA());
            cell = row.createCell(14);
            cell.setCellValue(dtos.get(i).getAllProdOrderNumber());
            cell = row.createCell(15);
            cell.setCellValue(dtos.get(i).getOptionInfo());
        }

        for(int i = 0; i < 16; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename=example.xlsx");

        try{
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // released, released_at 설정
        deliveryReadyService.releasedDeliveryReadyItem(viewDtos);
    }
}
