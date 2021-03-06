package com.example.file.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.example.file.model.message.Message;
import com.example.file.model.naver_order.dto.NaverOrderAssembledDto;
import com.example.file.service.naver_order.NaverOrderExcelUploadService;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/naver-order")
public class NaverOrderExcelUploadApiController {

    @Autowired
    NaverOrderExcelUploadService naverOrderExcelUploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) throws IOException {
        Message message = new Message();

        // file extension check.
        try{
            naverOrderExcelUploadService.isExcelFile(file);
        } catch(Exception e){
            message.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            message.setMessage("file_extension_error");
            message.setMemo("This is not an excel file.");
            return new ResponseEntity<>(message, message.getStatus());
        }

        try{
            message.setData(naverOrderExcelUploadService.uploadExcelFile(file));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @PostMapping("/download")
    public void downloadExcelFile(HttpServletResponse response, @RequestBody List<NaverOrderAssembledDto> dto) {
        Workbook workbook = new XSSFWorkbook();     // .xlsx
        Sheet sheet = workbook.createSheet("?????????");
        Row row = null;
        Cell cell = null;
        int rowNum = 0;

        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("????????????");
        cell = row.createCell(1);
        cell.setCellValue("??????????????????");
        cell = row.createCell(2);
        cell.setCellValue("????????????");
        cell = row.createCell(3);
        cell.setCellValue("????????????1");
        cell = row.createCell(4);
        cell.setCellValue("????????????");
        cell = row.createCell(5);
        cell.setCellValue("??????");
        cell = row.createCell(6);
        cell.setCellValue("???????????????");
        cell = row.createCell(7);
        cell.setCellValue("?????????1");
        cell = row.createCell(8);
        cell.setCellValue("???????????????(??????)");
        cell = row.createCell(9);
        cell.setCellValue("????????????1(??????)");
        cell = row.createCell(10);
        cell.setCellValue("????????????1");
        cell = row.createCell(11);
        cell.setCellValue("??????????????????");
        cell = row.createCell(12);
        cell.setCellValue("????????????1");
        cell = row.createCell(13);
        cell.setCellValue("???????????????");
        cell = row.createCell(14);
        cell.setCellValue("??????(A??????)");
        cell = row.createCell(15);
        cell.setCellValue("??? ??????????????????");

        for (int i=0; i<dto.size(); i++) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(dto.get(i).getOrderNumber());
            cell = row.createCell(1);
            cell.setCellValue(dto.get(i).getProdOrderNumber());
            cell = row.createCell(2);
            cell.setCellValue(dto.get(i).getReceiver());
            cell = row.createCell(3);
            cell.setCellValue(dto.get(i).getReceiverContact1());
            cell = row.createCell(4);
            cell.setCellValue(dto.get(i).getZipCode());
            cell = row.createCell(5);
            cell.setCellValue(dto.get(i).getDestination());
            cell = row.createCell(6);
            cell.setCellValue(dto.get(i).getTranportNumber());
            cell = row.createCell(7);
            cell.setCellValue(dto.get(i).getProdName1());
            cell = row.createCell(8);
            cell.setCellValue(dto.get(i).getSender());
            cell = row.createCell(9);
            cell.setCellValue(dto.get(i).getSenderContact1());
            cell = row.createCell(10);
            cell.setCellValue(dto.get(i).getProdDetailInfos().get(0).getProdDetail1());     // ???????????? ???????????? ???
            cell = row.createCell(11);
            cell.setCellValue(dto.get(i).getProdDetailInfos().get(0).getUnit1());       // ???????????? ???????????? ???
            cell = row.createCell(12);
            cell.setCellValue(dto.get(i).getProdDetailInfos().get(0).getOptionManageCode());        // ???????????? ???????????? ???
            cell = row.createCell(13);
            cell.setCellValue(dto.get(i).getDeliveryMessage());
            cell = row.createCell(14);
            cell.setCellValue(dto.get(i).getUnitA());
            cell = row.createCell(15);
            cell.setCellValue(dto.get(i).getProdOrderNumbers());
        }

        for(int i = 0; i < 12; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename=example.xlsx");   // ????????? ?????? ??????

        try{
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/uploadExcelsToCloud")
    public ResponseEntity<?> uploadExcelFilesToCloud(@RequestParam("files") List<MultipartFile> files) {
        Message message = new Message();

        // file extension check.
        try{
            naverOrderExcelUploadService.isExcelFile(files);
        } catch(Exception e){
            message.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            message.setMessage("file_extension_error");
            message.setMemo("This is not an image file.");
            return new ResponseEntity<>(message, message.getStatus());
        }

        try{
            message.setData(naverOrderExcelUploadService.uploadFilesToCloud(files));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }
}
