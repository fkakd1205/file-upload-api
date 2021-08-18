package com.example.file.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.example.file.model.excel_upload.NaverOrderGetDto;
import com.example.file.model.message.Message;
import com.example.file.service.naver_order.OrderExcelUploadService;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/order-excel")
@Slf4j
public class OrderExcelUploadApiController {
    
    @Autowired
    private OrderExcelUploadService orderExcelUploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) throws IOException {
        Message message = new Message();

        // file extension check.
        try{
            orderExcelUploadService.isExcelFile(file);
        } catch(Exception e){
            message.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            message.setMessage("file_extension_error");
            message.setMemo("This is not an excel file.");
            return new ResponseEntity<>(message, message.getStatus());
        }

        try{
            message.setData(orderExcelUploadService.uploadExcelFile(file));
            message.setStatus(HttpStatus.OK);
            message.setMessage("success");
        } catch(Exception e) {
            message.setStatus(HttpStatus.BAD_REQUEST);
            message.setMessage("error");
        }

        return new ResponseEntity<>(message, message.getStatus());
    }

    @PostMapping("/download")
    public void downloadExcelFile(HttpServletResponse response, @RequestBody List<NaverOrderGetDto> dto) {
        Workbook workbook = new XSSFWorkbook();     // .xlsx
        Sheet sheet = workbook.createSheet("발주서");
        Row row = null;
        Cell cell = null;
        int rowNum = 0;

        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("받는사람");
        cell = row.createCell(1);
        cell.setCellValue("전화번호1");
        cell = row.createCell(2);
        cell.setCellValue("우편번호");
        cell = row.createCell(3);
        cell.setCellValue("주소");
        cell = row.createCell(4);
        cell.setCellValue("운송장번호");
        cell = row.createCell(5);
        cell.setCellValue("상품명1");
        cell = row.createCell(6);
        cell.setCellValue("보내는사람(지정)");
        cell = row.createCell(7);
        cell.setCellValue("전화번호1(지정)");
        cell = row.createCell(8);
        cell.setCellValue("상품상세1");
        cell = row.createCell(9);
        cell.setCellValue("내품수량1");
        cell = row.createCell(10);
        cell.setCellValue("배송메시지");
        cell = row.createCell(11);
        cell.setCellValue("수량(A타입)");

        for (int i=0; i<dto.size(); i++) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(dto.get(i).getReceiver());
            cell = row.createCell(1);
            cell.setCellValue(dto.get(i).getReceiverContact1());
            cell = row.createCell(2);
            cell.setCellValue(dto.get(i).getZipCode());
            cell = row.createCell(3);
            cell.setCellValue(dto.get(i).getDestination());
            cell = row.createCell(4);
            cell.setCellValue(dto.get(i).getTranportNumber());
            cell = row.createCell(5);
            cell.setCellValue(dto.get(i).getProdName1());
            cell = row.createCell(6);
            cell.setCellValue(dto.get(i).getSender());
            cell = row.createCell(7);
            cell.setCellValue(dto.get(i).getSenderContact1());
            cell = row.createCell(8);
            cell.setCellValue(dto.get(i).getProdDetail1());
            cell = row.createCell(9);
            cell.setCellValue(dto.get(i).getUnit1());
            cell = row.createCell(10);
            cell.setCellValue(dto.get(i).getDeliveryMessage());
            cell = row.createCell(11);
            cell.setCellValue(dto.get(i).getUnitA());
        }

        for(int i = 0; i < 12; i++){
            sheet.autoSizeColumn(i);
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("[yy.MM.dd]");

        String newFileName = date.format(calendar.getTime()).toString() + "Order.xlsx";

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + newFileName + "\"");   // 파일명은 클라이언트에서 설정해야 함.

        try{
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
