package com.example.file.service.delivery_ready;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.file.model.delivery_ready.dto.DeliveryReadyItemExcelFormDto;
import com.example.file.model.delivery_ready.dto.DeliveryReadyItemOptionCodeInfo;
import com.example.file.model.delivery_ready.dto.DeliveryReadyItemViewDto;
import com.example.file.model.delivery_ready.entity.DeliveryReadyFileEntity;
import com.example.file.model.delivery_ready.entity.DeliveryReadyItemEntity;
import com.example.file.model.delivery_ready.proj.DeliveryReadyItemViewProj;
import com.example.file.model.delivery_ready.repository.DeliveryReadyFileRepository;
import com.example.file.model.delivery_ready.repository.DeliveryReadyItemRepository;
import com.example.file.model.file_upload.FileUploadResponse;

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
public class DeliveryReadyService {

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
    private DeliveryReadyFileRepository deliveryReadyFileRepository;

    @Autowired
    private DeliveryReadyItemRepository deliveryReadyItemRepository;
    
    // Excel file extension.
    private final List<String> EXTENSIONS_EXCEL = Arrays.asList("xlsx", "xls");

    public void isExcelFile(MultipartFile file) throws IOException{
        String extension = FilenameUtils.getExtension(file.getOriginalFilename().toLowerCase());

        if(EXTENSIONS_EXCEL.contains(extension)){
            return;
        }
        throw new IOException("엑셀파일만 업로드 해주세요.");
    }

    public List<DeliveryReadyItemEntity> uploadDeliveryReadyExcelFile(MultipartFile file) throws IOException {
        Workbook workbook = null;
        try{
            workbook = WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

        Sheet sheet = workbook.getSheetAt(0);
        List<DeliveryReadyItemEntity> dtos = getDeliveryReadyExcelForm(sheet);

        return dtos;
    }

    // AWS S3 setting.
    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public FileUploadResponse storeDeliveryReadyExcelFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String uploadPath = bucket + "/naver-order";

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(file.getSize());

        s3Client.putObject(new PutObjectRequest(uploadPath, fileName, file.getInputStream(), objMeta)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        // DeliveryReadyFileEntity 생성
        DeliveryReadyFileEntity fileEntity = createDeliveryReadyFileEntity(s3Client.getUrl(uploadPath, fileName).toString(), fileName, (int)file.getSize());

        // DeliveryReadyItemEntity 생성
        createDeliveryReadyItemData(file, fileEntity);
                                                      
        return new FileUploadResponse(fileName, s3Client.getUrl(uploadPath, fileName).toString(), file.getContentType(), file.getSize());
    }

    public DeliveryReadyFileEntity createDeliveryReadyFileEntity(String filePath, String fileName, Integer fileSize) {
        DeliveryReadyFileEntity fileEntity = new DeliveryReadyFileEntity();
        Date date = Calendar.getInstance().getTime();

        fileEntity.setId(UUID.randomUUID()).setFilePath(filePath).setFileName(fileName)
                .setFileSize(fileSize).setFileExtension(FilenameUtils.getExtension(fileName))
                .setCreatedAt(date).setCreatedBy(UUID.randomUUID()).setDeleted(false);

        return deliveryReadyFileRepository.save(fileEntity);
    }

    public void createDeliveryReadyItemData(MultipartFile file, DeliveryReadyFileEntity fileEntity) throws IllegalArgumentException {

        Workbook workbook = null;
        try{
            workbook = WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

        Sheet sheet = workbook.getSheetAt(0);
        getDeliveryReadyExcelData(sheet, fileEntity);
    }

    private void getDeliveryReadyExcelData(Sheet worksheet, DeliveryReadyFileEntity fileEntity) {
        List<DeliveryReadyItemEntity> dataList = new ArrayList<>();

        Set<String> storedProdOrderNumber = deliveryReadyItemRepository.findAllProdOrderNumber();   // 상품 주문번호로 중복데이터를 구분

        for(int i = 2; i < worksheet.getPhysicalNumberOfRows(); i++) {
            Row row = worksheet.getRow(i);

            DeliveryReadyItemEntity data = new DeliveryReadyItemEntity();

            data.setId(UUID.randomUUID())
                .setProdOrderNumber(row.getCell(0).getStringCellValue())
                .setOrderNumber(row.getCell(1).getStringCellValue())
                .setSalesChannel(row.getCell(7).getStringCellValue())
                .setBuyer(row.getCell(8).getStringCellValue())
                .setBuyerId(row.getCell(9).getStringCellValue())
                .setReceiver(row.getCell(10).getStringCellValue())
                .setPaymentDate(row.getCell(14).getDateCellValue())
                .setProdNumber(row.getCell(15).getStringCellValue())
                .setProdName(row.getCell(16).getStringCellValue())
                .setOptionInfo(row.getCell(18).getStringCellValue())
                .setOptionManagementCode(row.getCell(19) != null ? row.getCell(19).getStringCellValue() : "")
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
                .setOrderDateTime(row.getCell(56).getDateCellValue())
                .setReleased(false)
                .setCreatedAt(fileEntity.getCreatedAt())
                .setDeliveryReadyFileCid(fileEntity.getCid());

            // 상품주문번호가 중복되지 않는다면
            if(storedProdOrderNumber.add(data.getProdOrderNumber())){
                dataList.add(data);
            }
        }

        deliveryReadyItemRepository.saveAll(dataList);
    }

    private List<DeliveryReadyItemEntity> getDeliveryReadyExcelForm(Sheet worksheet) {
        List<DeliveryReadyItemEntity> dataList = new ArrayList<>();

        for(int i = 2; i < worksheet.getPhysicalNumberOfRows(); i++) {
            Row row = worksheet.getRow(i);

            DeliveryReadyItemEntity data = new DeliveryReadyItemEntity();

            data.setId(UUID.randomUUID())
                .setProdOrderNumber(row.getCell(0).getStringCellValue())
                .setOrderNumber(row.getCell(1).getStringCellValue())
                .setSalesChannel(row.getCell(7).getStringCellValue())
                .setBuyer(row.getCell(8).getStringCellValue())
                .setBuyerId(row.getCell(9).getStringCellValue())
                .setReceiver(row.getCell(10).getStringCellValue())
                .setPaymentDate(row.getCell(14).getDateCellValue())
                .setProdNumber(row.getCell(15).getStringCellValue())
                .setProdName(row.getCell(16).getStringCellValue())
                .setOptionInfo(row.getCell(18).getStringCellValue())
                .setOptionManagementCode(row.getCell(19) != null ? row.getCell(19).getStringCellValue() : "")
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

    public List<DeliveryReadyItemViewProj> getDeliveryReadyViewUnreleasedData() {
        return deliveryReadyItemRepository.findAllReleased(false);
    }

    public List<DeliveryReadyItemViewProj> getDeliveryReadyViewReleasedData(String currentDate) {
        List<DeliveryReadyItemViewProj> releasedItems = deliveryReadyItemRepository.findAllReleased(true);
        List<DeliveryReadyItemViewProj> todayReleasedItem = new ArrayList<>();

        for(DeliveryReadyItemViewProj releasedItem : releasedItems){
            Date storedReleasedAt = releasedItem.getDeliveryReadyItem().getReleasedAt();

            // UTC시간을 KST로 변경
            String releasedAt = changeLocalDate(storedReleasedAt);

            // 해당 날짜의 엑셀 데이터 추출
            if(releasedAt.contains(currentDate)){
                todayReleasedItem.add(releasedItem);
            }
        }

        return todayReleasedItem;
    }

    public List<DeliveryReadyItemViewProj> getDeliveryReadyViewReleased(String date1, String date2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null;
        Date endDate = null;

        try{
            startDate = dateFormat.parse(date1);
            endDate = dateFormat.parse(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<DeliveryReadyItemViewProj> releasedItems = deliveryReadyItemRepository.findSelectedReleased(true, startDate, endDate);
        
        return releasedItems;
    }

    public String changeLocalDate(Date storedReleasedAt) {
        LocalDateTime currentDate = LocalDateTime.ofInstant(storedReleasedAt.toInstant(), ZoneId.systemDefault());

        return currentDate.toString();
    }

    @Transactional
    public void releasedDeliveryReadyItem(List<DeliveryReadyItemViewDto> dtos) {
        Date date = Calendar.getInstance().getTime();

        List<Integer> cidList = new ArrayList<>();
        
        for(DeliveryReadyItemViewDto dto : dtos){
            cidList.add(dto.getDeliveryReadyItem().getCid());
        }
        deliveryReadyItemRepository.updateReleasedAtByCid(cidList, date);
    }

    public void updateReleasedDeliveryReadyItem(UUID itemId) {
        deliveryReadyItemRepository.findByItemId(itemId).ifPresentOrElse(item -> {
            item.setReleased(false).setReleasedAt(null);

            deliveryReadyItemRepository.save(item);
        }, null);;
    }

    public List<DeliveryReadyItemOptionCodeInfo> searchDeliveryReadyItemProductInfo() {
        return deliveryReadyItemRepository.findByOptionInfo();
    }

    public List<DeliveryReadyItemExcelFormDto> getFromDtoByEntity(List<DeliveryReadyItemEntity> entities) {
        List<DeliveryReadyItemExcelFormDto> formDtos = new ArrayList<>();

        for(DeliveryReadyItemEntity entity : entities){
            DeliveryReadyItemExcelFormDto formDto = new DeliveryReadyItemExcelFormDto();

            formDto.setProdOrderNumber(entity.getProdOrderNumber()).setOrderNumber(entity.getOrderNumber())
                    .setReceiver(entity.getReceiver()).setReceiverContact1(entity.getReceiverContact1())
                    .setZipCode(entity.getZipCode()).setDestination(entity.getDestination())
                    .setTransportNumber("").setProdName(entity.getProdName())
                    .setSender("스토어명").setSenderContact1("070-0000-0000").setOptionInfo(entity.getOptionInfo())
                    .setOptionManagementCode(entity.getOptionManagementCode()).setUnit(entity.getUnit())
                    .setDeliveryMessage(entity.getDeliveryMessage()).setUnitA("").setAllProdOrderNumber(entity.getProdNumber()).setDuplication(false);

            formDtos.add(formDto);
        }

        return formDtos;
    }

    public List<DeliveryReadyItemExcelFormDto> changeDeliveryReadyItem(List<DeliveryReadyItemViewDto> dtos) {
        List<DeliveryReadyItemEntity> entities = new ArrayList<>();

        // DeliveryReadyItemViewDto에서 DeliveryReadyItemEntity만 뽑아낸다
        for(DeliveryReadyItemViewDto viewDto : dtos) {
            entities.add(viewDto.getDeliveryReadyItem());
        }

        // DeliveryReadyItemEntity로 DeliveryReadyItemExcelFromDto를 만든다
        List<DeliveryReadyItemExcelFormDto> formDtos = getFromDtoByEntity(entities);

        return changeDuplicationEntity(formDtos);
    }

    public List<DeliveryReadyItemExcelFormDto> changeDuplicationEntity(List<DeliveryReadyItemExcelFormDto> entities) {
        List<DeliveryReadyItemExcelFormDto> newOrderList = new ArrayList<>();

        // 주문번호 > 받는사람 > 상품명 > 상품상세 정렬
        entities.sort(Comparator.comparing(DeliveryReadyItemExcelFormDto::getOrderNumber)
                                .thenComparing(DeliveryReadyItemExcelFormDto::getReceiver)
                                .thenComparing(DeliveryReadyItemExcelFormDto::getProdName)
                                .thenComparing(DeliveryReadyItemExcelFormDto::getOptionInfo));

        Set<String> optionSet = new HashSet<>();        // 받는사람 + 주소 + 상품명 + 상품상세

        for(int i = 0; i < entities.size(); i++){
            StringBuilder sb = new StringBuilder();
            sb.append(entities.get(i).getReceiver());
            sb.append(entities.get(i).getDestination());
            sb.append(entities.get(i).getProdName());
            sb.append(entities.get(i).getOptionInfo());

            StringBuilder receiverSb = new StringBuilder();
            receiverSb.append(entities.get(i).getReceiver());
            receiverSb.append(entities.get(i).getReceiverContact1());
            receiverSb.append(entities.get(i).getDestination());

            String resultStr = sb.toString();
            String receiverStr = receiverSb.toString();
            int prevOrderIdx = newOrderList.size()-1;   // 추가되는 데이터 리스트의 마지막 index

            // 받는사람 + 번호 + 주소 : 중복인 경우
            if(!optionSet.add(receiverStr)){
                newOrderList.get(prevOrderIdx).setDuplication(true);
                entities.get(i).setDuplication(true);
            }

            // 받는사람 + 주소 + 상품명 + 상품상세 : 중복인 경우
            if(!optionSet.add(resultStr)){
                DeliveryReadyItemExcelFormDto prevProd = newOrderList.get(prevOrderIdx);
                DeliveryReadyItemExcelFormDto currentProd = entities.get(i);

                newOrderList.get(prevOrderIdx).setUnit(prevProd.getUnit() + currentProd.getUnit());     // 중복데이터의 수량을 더한다
                newOrderList.get(prevOrderIdx).setAllProdOrderNumber(prevProd.getProdOrderNumber() + " / " + currentProd.getProdOrderNumber());     // 총 상품번호 수정
            }else{
                newOrderList.add(entities.get(i));
            }
        }

        return newOrderList;
    }

    public void deleteOneDeliveryReadyViewData(UUID itemId) {
        deliveryReadyItemRepository.findByItemId(itemId).ifPresent(item -> {
            deliveryReadyItemRepository.delete(item);
        });
    }
}