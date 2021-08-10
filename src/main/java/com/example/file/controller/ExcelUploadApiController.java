package com.example.file.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.example.file.model.excel_upload.ExcelUploadGetDto;
import com.example.file.model.message.Message;
import com.example.file.service.excel_upload.ExcelUploadService;

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
@RequestMapping("/api/v1/excel")
public class ExcelUploadApiController {
    
    @Autowired
    private ExcelUploadService excelUploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) throws IOException {
        Message message = new Message();

        // file extension check.
        try{
            excelUploadService.isExcelFile(file);
        } catch(Exception e){
            message.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            message.setMessage("file_extension_error");
            message.setMemo("This is not an excel file.");
            return new ResponseEntity<>(message, message.getStatus());
        }

        try{
            message.setData(excelUploadService.uploadExcelFile(file));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @PostMapping("/download")
    public void downloadExcelFile(HttpServletResponse response, @RequestBody List<ExcelUploadGetDto> dto) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("첫번째 시트");
        Row row = null;
        Cell cell = null;
        int rowNum = 0;

        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("구매자명");
        cell = row.createCell(1);
        cell.setCellValue("상품명");
        cell = row.createCell(2);
        cell.setCellValue("상품코드");
        cell = row.createCell(3);
        cell.setCellValue("배송지");
        cell = row.createCell(4);
        cell.setCellValue("구매자 연락처");
        cell = row.createCell(5);
        cell.setCellValue("배송 메세지");

        for (int i=0; i<dto.size(); i++) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(dto.get(i).getBuyer());
            cell = row.createCell(1);
            cell.setCellValue(dto.get(i).getProductName());
            cell = row.createCell(2);
            cell.setCellValue(dto.get(i).getProductCode());
            cell = row.createCell(3);
            cell.setCellValue(dto.get(i).getDestination());
            cell = row.createCell(4);
            cell.setCellValue(dto.get(i).getBuyerContact());
            cell = row.createCell(5);
            cell.setCellValue(dto.get(i).getDeliveryMessage());
        }

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename=example.xlsx");

        try{
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
