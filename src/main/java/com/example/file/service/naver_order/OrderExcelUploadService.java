package com.example.file.service.naver_order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.file.model.naver_order.dto.NaverOrderGetDto;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrderExcelUploadService {
    // Excel file extension.
    private final List<String> EXTENSIONS_EXCEL = Arrays.asList("xlsx", "xls");

    public List<NaverOrderGetDto> uploadExcelFile(MultipartFile file) throws IllegalArgumentException {

        Workbook workbook = null;
        try{
            workbook = WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

        Sheet sheet = workbook.getSheetAt(0);
        List<NaverOrderGetDto> dtos = getExcelForm(sheet);
        
        // 받는사람 > 주소 > 상품명1 > 상품 상세1
        dtos.sort(Comparator.comparing(NaverOrderGetDto::getReceiver)
                    .thenComparing(NaverOrderGetDto::getDestination)
                    .thenComparing(NaverOrderGetDto::getProdName1)
                    .thenComparing(NaverOrderGetDto::getProdDetail1));

        // 중복 데이터 가공
        List<NaverOrderGetDto> newDtos = changeOrderDtos(dtos);

        return newDtos;
    }

    public List<NaverOrderGetDto> changeOrderDtos(List<NaverOrderGetDto> dtos){
        List<NaverOrderGetDto> newOrderList = new ArrayList<>();
        List<NaverOrderGetDto> resultOrderList = new ArrayList<>();

        // 받는사람 + 주소 + 상품명1 + 상품상세1 : 중복체크
        duplicationCheckProdName(dtos, newOrderList);

        // 상품 수량 내림차순
        newOrderList.sort(Comparator.comparing(NaverOrderGetDto::getUnit1).reversed());

        // 받는사람, 상품명1 오름차순
        newOrderList.sort(Comparator.comparing(NaverOrderGetDto::getReceiver)
                        .thenComparing(NaverOrderGetDto::getProdName1));

        // 받는사람 + 주소 + 상품명1 : 중복체크
        duplicationCheckProdDetail1(newOrderList, resultOrderList);

        return resultOrderList;
    }
    
    public void duplicationCheckProdName(List<NaverOrderGetDto> dtos, List<NaverOrderGetDto> newOrderList){
        Set<String> orderSet = new HashSet<>();

        for(int i = 0; i < dtos.size(); i++){
            StringBuilder sb = new StringBuilder();
            sb.append(dtos.get(i).getReceiver());
            sb.append(dtos.get(i).getDestination());
            sb.append(dtos.get(i).getProdName1());
            sb.append(dtos.get(i).getProdDetail1());

            String resultStr = sb.toString();
            int prevOrderIdx = newOrderList.size()-1;

            // 받는사람 + 주소 + 상품명1 + 상품상세1 : 중복된다면 수량 +1
            if(!orderSet.add(resultStr)){
                newOrderList.get(prevOrderIdx).setUnit1(newOrderList.get(prevOrderIdx).getUnit1() + dtos.get(i).getUnit1());
            }else{
                NaverOrderGetDto dto = new NaverOrderGetDto();
                dto.setReceiver(dtos.get(i).getReceiver());
                dto.setReceiverContact1(dtos.get(i).getReceiverContact1());
                dto.setZipCode(dtos.get(i).getZipCode());
                dto.setDestination(dtos.get(i).getDestination());
                dto.setTranportNumber(dtos.get(i).getTranportNumber());
                dto.setProdName1(dtos.get(i).getProdName1());
                dto.setSender(dtos.get(i).getSender());
                dto.setSenderContact1(dtos.get(i).getSenderContact1());
                dto.setProdDetail1(dtos.get(i).getProdDetail1());
                dto.setUnit1(dtos.get(i).getUnit1());
                dto.setDeliveryMessage(dtos.get(i).getDeliveryMessage());
                dto.setUnitA(dtos.get(i).getUnitA());

                newOrderList.add(dto);
            }
        }
    }

    public void duplicationCheckProdDetail1(List<NaverOrderGetDto> newOrderList, List<NaverOrderGetDto> resultOrderList){
        Set<String> orderSet = new HashSet<>();

        for(int i = 0; i < newOrderList.size(); i++){
            StringBuilder sb = new StringBuilder();
            sb.append(newOrderList.get(i).getReceiver());
            sb.append(newOrderList.get(i).getDestination());
            sb.append(newOrderList.get(i).getProdName1());

            String resultStr = sb.toString();
            int prevOrderIdx = resultOrderList.size()-1;

            // 받는사람 + 주소 + 상품명1 : 상품상세1(옵션) 추가
            if(!orderSet.add(resultStr) && (resultOrderList.get(prevOrderIdx).getUnit1() == newOrderList.get(i).getUnit1())){
                resultOrderList.get(prevOrderIdx).setProdDetail1(resultOrderList.get(prevOrderIdx).getProdDetail1() + newOrderList.get(i).getProdDetail1());
            }else{
                resultOrderList.add(newOrderList.get(i));
            }
        }
    }

    private List<NaverOrderGetDto> getExcelForm(Sheet worksheet) {
        List<NaverOrderGetDto> dataList = new ArrayList<>();
        
        for(int i = 2; i < worksheet.getPhysicalNumberOfRows(); i++){
            Row row = worksheet.getRow(i);

            NaverOrderGetDto data = new NaverOrderGetDto();
            data.setReceiver(row.getCell(10).getStringCellValue());
            data.setReceiverContact1(row.getCell(40).getStringCellValue());
            data.setZipCode(row.getCell(44).getStringCellValue());
            data.setDestination(row.getCell(42).getStringCellValue());
            data.setTranportNumber("");
            data.setProdName1(row.getCell(16).getStringCellValue());
            data.setSender("스토어이름");
            data.setSenderContact1("070-0000-0000");
            data.setProdDetail1("🎁" + row.getCell(18).getStringCellValue());      // 다운로드 후 직접 수정해야 하는 항목
            data.setUnit1((int)(row.getCell(20).getNumericCellValue()));
            data.setDeliveryMessage("");        // 45 행 -> null 체크 or 원래 빈 값인지 확인
            data.setUnitA("");
            
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
