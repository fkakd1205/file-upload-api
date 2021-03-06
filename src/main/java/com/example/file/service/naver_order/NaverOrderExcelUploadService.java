package com.example.file.service.naver_order;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.file.model.naver_order.dto.NaverOrderAssembledDto;
import com.example.file.model.naver_order.dto.NaverOrderProdDetailInfo;
import com.example.file.model.naver_order.dto.NaverOrderReadDto;
import com.example.file.model.naver_order.entity.ExcelDataEntity;
import com.example.file.payload.FileUploadResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NaverOrderExcelUploadService {

    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${file.upload-dir}")
    String fileLocation;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    @Autowired
    private DetailDataService detailDataService;

    @Autowired
    private ExcelDataService excelDataSerivce;

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
        List<ExcelDataEntity> entity = getExcelData(sheet);

        // ExcelDataEntity ??????
        excelDataSerivce.createExcelData(entity);

        // ???????????? > ???????????? > ?????????1 > ????????????1 ???????????? ??????
        dtos.sort(Comparator.comparing(NaverOrderReadDto::getOrderNumber)
                    .thenComparing(NaverOrderReadDto::getReceiver)
                    .thenComparing(NaverOrderReadDto::getProdName1)
                    .thenComparing(NaverOrderReadDto::getProdDetail1));

        // ?????? ????????? ??????
        List<NaverOrderAssembledDto> newDtos = changeOrderDtos(dtos);

        newDtos.sort(Comparator.comparing(NaverOrderAssembledDto::getReceiver));

        return newDtos;
    }

    public List<NaverOrderAssembledDto> changeOrderDtos(List<NaverOrderReadDto> dtos){
        List<NaverOrderAssembledDto> newOrderList = new ArrayList<>();

        Set<String> orderNumberSet = new HashSet<>();       // ???????????? - Set
        Set<String> optionSet = new HashSet<>();        // ???????????? + ?????? + ?????????1 + ????????????1 - Set

        int index = 0;

        for(int i = 0; i < dtos.size(); i++){
            StringBuilder sb = new StringBuilder();
            sb.append(dtos.get(i).getReceiver());
            sb.append(dtos.get(i).getDestination());
            sb.append(dtos.get(i).getProdName1());
            sb.append(dtos.get(i).getProdDetail1());

            String resultStr = sb.toString();
            int prevOrderIdx = newOrderList.size()-1;       // ???????????? ????????? ???????????? ????????? index

            // ??????????????? ???????????????
            if(!orderNumberSet.add(dtos.get(i).getOrderNumber())){
                // ?????? ?????????????????? ????????????
                List<NaverOrderProdDetailInfo> detailInfo =  newOrderList.get(prevOrderIdx).getProdDetailInfos();
                NaverOrderProdDetailInfo optionInfo = new NaverOrderProdDetailInfo();

                // ???????????? + ?????? + ?????????1 + ????????????1 : ??????????????? ????????? ?????????
                if(!optionSet.add(resultStr)){
                    int prevProdUnit = newOrderList.get(prevOrderIdx).getProdDetailInfos().get(index).getUnit1();
                    int currentProdUnit = dtos.get(i).getUnit1();

                    newOrderList.get(prevOrderIdx).getProdDetailInfos().get(index).setUnit1(prevProdUnit + currentProdUnit);
                }else{
                    optionInfo.setProdDetail1(dtos.get(i).getProdDetail1());
                    optionInfo.setUnit1(dtos.get(i).getUnit1());

                    detailInfo.add(optionInfo);
                    index++;    // ???????????? + ?????? + ?????????1 : ???????????? ????????? ????????? index ??????
                }
                newOrderList.get(prevOrderIdx).setProdDetailInfos(detailInfo);
                newOrderList.get(prevOrderIdx).setProdOrderNumbers(newOrderList.get(prevOrderIdx).getProdOrderNumbers() + " / " + dtos.get(i).getProdOrderNumber());
            }else{
                // ????????? ?????? ????????? ??????
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

                optionSet.add(resultStr);       // ????????? ????????? ????????????????????? Set??? ??????
                index = 0;      // ????????? ????????? ????????? ????????? index ?????????
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
            data.setSender("???????????????");
            data.setSenderContact1("070-0000-0000");
            data.setProdDetail1(row.getCell(18).getStringCellValue());      // ???????????? ??? ?????? ???????????? ?????? ??????
            data.setOptionManageCode(row.getCell(13) != null ? row.getCell(13).getStringCellValue() : "");
            data.setUnit1((int)(row.getCell(20).getNumericCellValue()));
            data.setDeliveryMessage("");        // 45 ??? -> null ?????? or ?????? ??? ????????? ??????
            data.setUnitA("");
            
            dataList.add(data);
        }

        return dataList;
    }

    private List<ExcelDataEntity> getExcelData(Sheet worksheet) {
        List<ExcelDataEntity> dataList = new ArrayList<>();

        for(int i = 2; i < worksheet.getPhysicalNumberOfRows(); i++) {
            Row row = worksheet.getRow(i);

            ExcelDataEntity data = new ExcelDataEntity();

            data.setProdOrderNumber(row.getCell(0).getStringCellValue())
                .setOrderNumber(row.getCell(1).getStringCellValue())
                .setSalesChannel(row.getCell(7).getStringCellValue())
                .setBuyer(row.getCell(8).getStringCellValue())
                .setBuyerId(row.getCell(9).getStringCellValue())
                .setReceiver(row.getCell(10).getStringCellValue())
                .setPaymentDate(row.getCell(14).getDateCellValue())
                .setProdNumber(row.getCell(15).getStringCellValue())
                .setProdName(row.getCell(16).getStringCellValue())
                .setOptionInfo(row.getCell(18).getStringCellValue())
                .setOptionManageCode(row.getCell(19) != null ? row.getCell(19).getStringCellValue() : "")
                .setUnit((int) row.getCell(20).getNumericCellValue())
                .setOrderConfirmationDate(row.getCell(27).getDateCellValue())
                .setShipmentDueDate(row.getCell(28).getDateCellValue())
                .setShipmentCostBundleNumber(row.getCell(32).getStringCellValue())
                .setSellerProdCode(row.getCell(37) != null ? row.getCell(37).getStringCellValue() : "")
                .setSellerInnerCode1(row.getCell(38) != null ? row.getCell(38).getStringCellValue() : "")
                .setSellerInnerCode2(row.getCell(39) != null ? row.getCell(39).getStringCellValue() : "")
                .setReceiverContact1(row.getCell(40).getStringCellValue())
                .setReceiverContact2(row.getCell(41) != null ? row.getCell(41).getStringCellValue() : "")
                .setDestination(row.getCell(42).getStringCellValue())
                .setBuyerContact(row.getCell(43).getStringCellValue())
                .setZipCode(row.getCell(44).getStringCellValue())
                .setDeliveryMessage(row.getCell(45) != null ? row.getCell(45).getStringCellValue() : "")
                .setReleaseArea(row.getCell(46).getStringCellValue())
                .setOrderDateTime(row.getCell(56).getDateCellValue());

            dataList.add(data);
        }

        return dataList;
    }

    public void isExcelFile(MultipartFile file) throws IOException{
        String extension = FilenameUtils.getExtension(file.getOriginalFilename().toLowerCase());

        if(EXTENSIONS_EXCEL.contains(extension)){
            return;
        }
        throw new IOException("??????????????? ????????? ????????????.");
    }

    public void isExcelFile(List<MultipartFile> files) throws IOException{
        for(MultipartFile file : files){
            isExcelFile(file);
        }
    }

    /**
     * AWS S3 setting.
     */
    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public FileUploadResponse uploadFileToCloud(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        bucket += "/naver-order";

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(file.getSize());

        s3Client.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), objMeta)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        // DetailDataEntity ??????
        detailDataService.createDetailData(s3Client.getUrl(bucket, fileName).toString(), fileName, file.getSize());
                                                      
        return new FileUploadResponse(fileName, s3Client.getUrl(bucket, fileName).toString(), file.getContentType(), file.getSize());
    }

    public List<FileUploadResponse> uploadFilesToCloud(List<MultipartFile> files) throws IOException {
        List<FileUploadResponse> uploadFiles = new ArrayList<>();

        for(MultipartFile file : files){
            uploadFiles.add(uploadFileToCloud(file));
        }

        return uploadFiles;
    }
}
