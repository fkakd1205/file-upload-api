package com.example.file.service.naver_order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.example.file.model.excel_upload.NaverOrderAssembledDto;
import com.example.file.model.excel_upload.NaverOrderGetDto;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NaverOrderExcelUploadService {

    // Excel file extension.
    // private final List<String> EXTENSIONS_EXCEL = Arrays.asList("xlsx", "xls");
    
    // public List<NaverOrderAssembledDto> uploadExcelFile(MultipartFile file) throws IllegalArgumentException {

    //     Workbook workbook = null;
    //     try{
    //         workbook = WorkbookFactory.create(file.getInputStream());
    //     } catch (Exception e) {
    //         throw new IllegalArgumentException();
    //     }

        // Sheet sheet = workbook.getSheetAt(0);
        // List<NaverOrderReadDto> dtos = getExcelForm(sheet);
        
        // // 주문번호 > 받는 사람 오름차순 정렬
        // dtos.sort(Comparator.comparing(NaverOrderReadDto::getOrderNumber)
        //             .thenComparing(NaverOrderReadDto::getReceiver));

        // // 중복 데이터 가공
        // List<NaverOrderAssembledDto> newDtos = changeOrderDtos(dtos);

    //     return newDtos;
    // }

    // private List<NaverOrderReadDto> getExcelForm(Sheet worksheet) {
    //     List<NaverOrderReadDto> dataList = new ArrayList<>();
        
    //     for(int i = 2; i < worksheet.getPhysicalNumberOfRows(); i++){
    //         Row row = worksheet.getRow(i);

    //         NaverOrderReadDto data = new NaverOrderReadDto();
    //         data.setReceiver(row.getCell(10).getStringCellValue());
    //         data.setReceiverContact1(row.getCell(40).getStringCellValue());
    //         data.setZipCode(row.getCell(44).getStringCellValue());
    //         data.setDestination(row.getCell(42).getStringCellValue());
    //         data.setTranportNumber("");
    //         data.setProdName1(row.getCell(16).getStringCellValue());
    //         data.setSender("스토어이름");
    //         data.setSenderContact1("070-0000-0000");
    //         data.setProdDetail1("🎁" + row.getCell(18).getStringCellValue());      // 다운로드 후 직접 수정해야 하는 항목
    //         data.setUnit1((int)(row.getCell(20).getNumericCellValue());
    //         data.setDeliveryMessage("");        // 45 행 -> null 체크 or 원래 빈 값인지 확인
    //         data.setUnitA("");
            
    //         dataList.add(data);
    //     }

    //     return dataList;
    // }

    public void isExcelFile(MultipartFile file) throws IOException{
        String extension = FilenameUtils.getExtension(file.getOriginalFilename().toLowerCase());

        if(EXTENSIONS_EXCEL.contains(extension)){
            return;
        }
        throw new IOException("엑셀파일만 업로드 해주세요.");
    }
}
