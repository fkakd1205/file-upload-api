package com.example.file.service.excel_upload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.file.model.excel_upload.ExcelUploadGetDto;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelUploadService {
    
    // Excel file extension.
    private final List<String> EXTENSIONS_EXCEL = Arrays.asList("xlsx", "xls");

    public List<ExcelUploadGetDto> uploadExcelFile(MultipartFile file) throws IllegalArgumentException {

        Workbook workbook = null;
        try{
            workbook = WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

        Sheet sheet = workbook.getSheetAt(0);
        List<ExcelUploadGetDto> dtos = getExcelForm(sheet);

        return dtos;
    }

    private List<ExcelUploadGetDto> getExcelForm(Sheet worksheet) {
        List<ExcelUploadGetDto> dataList = new ArrayList<>();
        
        for(int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++){
            Row row = worksheet.getRow(i);

            ExcelUploadGetDto data = new ExcelUploadGetDto();
            data.setBuyer(row.getCell(0).getStringCellValue());
            data.setProductName(row.getCell(1).getStringCellValue());
            data.setProductCode(row.getCell(2).getStringCellValue());
            data.setDestination(row.getCell(3).getStringCellValue());
            data.setBuyerContact(row.getCell(4).getStringCellValue());
            data.setDeliveryMessage(row.getCell(5).getStringCellValue());
            
            dataList.add(data);
        }

        return dataList;
    }

    public void isExcelFile(MultipartFile file) throws IOException{
        String extension = FilenameUtils.getExtension(file.getOriginalFilename().toLowerCase());

        if(EXTENSIONS_EXCEL.contains(extension)){
            return;
        }
        throw new IOException("엑셀파일만 업로드 해주세요.");
    }
}
