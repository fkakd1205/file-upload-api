package com.example.file.service.naver_order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.file.model.excel_upload.NaverOrderAssembledDto;
import com.example.file.model.excel_upload.NaverOrderProdDetailInfo;
import com.example.file.model.excel_upload.NaverOrderReadDto;

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
    private final List<String> EXTENSIONS_EXCEL = Arrays.asList("xlsx", "xls");
    
    public List<NaverOrderAssembledDto> uploadExcelFile(MultipartFile file) throws IllegalArgumentException {

        Workbook workbook = null;
        try{
            workbook = WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

        Sheet sheet = workbook.getSheetAt(0);
        List<NaverOrderReadDto> dtos = getExcelForm(sheet);
        
        // 주문번호 > 받는사람 > 상품명1 > 상품상세1 오름차순 정렬
        dtos.sort(Comparator.comparing(NaverOrderReadDto::getOrderNumber)
                    .thenComparing(NaverOrderReadDto::getReceiver)
                    .thenComparing(NaverOrderReadDto::getProdName1)
                    .thenComparing(NaverOrderReadDto::getProdDetail1));

        // 중복 데이터 가공
        List<NaverOrderAssembledDto> newDtos = changeOrderDtos(dtos);

        newDtos.sort(Comparator.comparing(NaverOrderAssembledDto::getReceiver));

        return newDtos;
    }

    public List<NaverOrderAssembledDto> changeOrderDtos(List<NaverOrderReadDto> dtos){
        List<NaverOrderAssembledDto> newOrderList = new ArrayList<>();

        Set<String> orderNumberSet = new HashSet<>();       // 주문번호 - Set
        Set<String> optionSet = new HashSet<>();        // 받는사람 + 주소 + 상품명1 + 상품상세1 - Set

        int index = 0;

        for(int i = 0; i < dtos.size(); i++){
            StringBuilder sb = new StringBuilder();
            sb.append(dtos.get(i).getReceiver());
            sb.append(dtos.get(i).getDestination());
            sb.append(dtos.get(i).getProdName1());
            sb.append(dtos.get(i).getProdDetail1());

            String resultStr = sb.toString();
            int prevOrderIdx = newOrderList.size()-1;       // 추가되는 데이터 리스트의 마지막 index

            // 주문번호가 동일하다면
            if(!orderNumberSet.add(dtos.get(i).getOrderNumber())){
                // 기존 옵션데이터를 가져온다
                List<NaverOrderProdDetailInfo> detailInfo =  newOrderList.get(prevOrderIdx).getProdDetailInfos();
                NaverOrderProdDetailInfo optionInfo = new NaverOrderProdDetailInfo();

                // 받는사람 + 주소 + 상품명1 + 상품상세1 : 중복된다면 수량을 더한다
                if(!optionSet.add(resultStr)){
                    int prevProdUnit = newOrderList.get(prevOrderIdx).getProdDetailInfos().get(index).getUnit1();
                    int currentProdUnit = dtos.get(i).getUnit1();

                    newOrderList.get(prevOrderIdx).getProdDetailInfos().get(index).setUnit1(prevProdUnit + currentProdUnit);
                }else{
                    optionInfo.setProdDetail1(dtos.get(i).getProdDetail1());
                    optionInfo.setUnit1(dtos.get(i).getUnit1());

                    detailInfo.add(optionInfo);
                    index++;    // 받는사람 + 주소 + 상품명1 : 중복되는 데이터 안에서 index 증가
                }
                newOrderList.get(prevOrderIdx).setProdDetailInfos(detailInfo);
                newOrderList.get(prevOrderIdx).setProdOrderNumbers(newOrderList.get(prevOrderIdx).getProdOrderNumbers() + " / " + dtos.get(i).getProdOrderNumber());
            }else{
                // 새로운 주문 데이터 등록
                NaverOrderAssembledDto dto = new NaverOrderAssembledDto();
                dto.setOrderNumber(dtos.get(i).getOrderNumber());
                dto.setProdOrderNumber(dtos.get(i).getProdOrderNumber());
                dto.setReceiver(dtos.get(i).getReceiver());
                dto.setReceiverContact1(dtos.get(i).getReceiverContact1());
                dto.setZipCode(dtos.get(i).getZipCode());
                dto.setDestination(dtos.get(i).getDestination());
                dto.setTranportNumber(dtos.get(i).getTranportNumber());
                dto.setProdName1(dtos.get(i).getProdName1());
                dto.setSender(dtos.get(i).getSender());
                dto.setSenderContact1(dtos.get(i).getSenderContact1());
                dto.setDeliveryMessage(dtos.get(i).getDeliveryMessage());
                dto.setUnitA(dtos.get(i).getUnitA());
                dto.setProdOrderNumbers(dtos.get(i).getProdOrderNumber());

                List<NaverOrderProdDetailInfo> detailInfo = new ArrayList<>();
                NaverOrderProdDetailInfo optionInfo = new NaverOrderProdDetailInfo();
                optionInfo.setProdDetail1(dtos.get(i).getProdDetail1());
                optionInfo.setOptionManageCode(dtos.get(i).getOptionManageCode());
                optionInfo.setUnit1(dtos.get(i).getUnit1());
                detailInfo.add(optionInfo);

                dto.setProdDetailInfos(detailInfo);
                newOrderList.add(dto);

                optionSet.add(resultStr);       // 새로운 옵션이 추가되었으므로 Set에 추가
                index = 0;      // 새로운 상품이 등록될 때마다 index 초기화
            }
        }

        return newOrderList;
    }

    private List<NaverOrderReadDto> getExcelForm(Sheet worksheet) {
        List<NaverOrderReadDto> dataList = new ArrayList<>();
        
        for(int i = 2; i < worksheet.getPhysicalNumberOfRows(); i++){
            Row row = worksheet.getRow(i);

            NaverOrderReadDto data = new NaverOrderReadDto();
            data.setOrderNumber(row.getCell(1).getStringCellValue());
            data.setProdOrderNumber(row.getCell(0).getStringCellValue());
            data.setReceiver(row.getCell(10).getStringCellValue());
            data.setReceiverContact1(row.getCell(40).getStringCellValue());
            data.setZipCode(row.getCell(44).getStringCellValue());
            data.setDestination(row.getCell(42).getStringCellValue());
            data.setTranportNumber("");
            data.setProdName1(row.getCell(16).getStringCellValue());
            data.setSender("스토어이름");
            data.setSenderContact1("070-0000-0000");
            data.setProdDetail1(row.getCell(18).getStringCellValue());      // 다운로드 후 직접 수정해야 하는 항목
            data.setOptionManageCode(row.getCell(13) != null ? row.getCell(13).getStringCellValue() : "");
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
