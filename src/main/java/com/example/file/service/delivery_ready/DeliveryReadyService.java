package com.example.file.service.delivery_ready;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.example.file.model.delivery_ready.dto.DeliveryReadyItemViewDto;
import com.example.file.model.delivery_ready.entity.DeliveryReadyFileEntity;
import com.example.file.model.delivery_ready.entity.DeliveryReadyItemEntity;
import com.example.file.model.delivery_ready.proj.DeliveryReadyItemOptionInfoProj;
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

@Service
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
        throw new IOException("??????????????? ????????? ????????????.");
    }

    // 1. -1 ?????? ?????????
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

    // 1. -2 ???????????? ?????? ????????? ??????
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

    
    // 2. -1 ?????? ????????????
    public FileUploadResponse storeDeliveryReadyExcelFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String uploadPath = bucket + "/naver-order";
        
        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(file.getSize());
        
        // 2. -2 AWS S3 ?????????
        s3Client.putObject(new PutObjectRequest(uploadPath, fileName, file.getInputStream(), objMeta)
        .withCannedAcl(CannedAccessControlList.PublicRead));
        
        // 3. -1 file DB??????
        // DeliveryReadyFileEntity ??????
        DeliveryReadyFileEntity fileEntity = createDeliveryReadyFileEntity(s3Client.getUrl(uploadPath, fileName).toString(), fileName, (int)file.getSize());
        
        // 3. -2 item ??????????????? ?????? ??? DB??????
        // DeliveryReadyItemEntity ??????
        createDeliveryReadyItemData(file, fileEntity);
        
        return new FileUploadResponse(fileName, s3Client.getUrl(uploadPath, fileName).toString(), file.getContentType(), file.getSize());
    }
    
    // 2. -2 AWS S3 ?????????
    // AWS S3 setting.
    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }
    
    // 3. -1 file DB??????
    public DeliveryReadyFileEntity createDeliveryReadyFileEntity(String filePath, String fileName, Integer fileSize) {
        DeliveryReadyFileEntity fileEntity = new DeliveryReadyFileEntity();
        Date date = Calendar.getInstance().getTime();

        fileEntity.setId(UUID.randomUUID()).setFilePath(filePath).setFileName(fileName)
                .setFileSize(fileSize).setFileExtension(FilenameUtils.getExtension(fileName))
                .setCreatedAt(date).setCreatedBy(UUID.randomUUID()).setDeleted(false);

        return deliveryReadyFileRepository.save(fileEntity);
    }

    // 3. -2 item ?????? ?????? ??? DB??????
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

    // 3. -3 ????????????????????? ???????????? ?????? ???????????? DB??? ??????
    private void getDeliveryReadyExcelData(Sheet worksheet, DeliveryReadyFileEntity fileEntity) {
        List<DeliveryReadyItemEntity> dataList = new ArrayList<>();

        Set<String> storedProdOrderNumber = deliveryReadyItemRepository.findAllProdOrderNumber();   // ?????? ??????????????? ?????????????????? ??????

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

            // ????????????????????? ???????????? ????????????
            if(storedProdOrderNumber.add(data.getProdOrderNumber())){
                dataList.add(data);
            }
        }

        deliveryReadyItemRepository.saveAll(dataList);
    }

    // 4. ?????? ????????? ??????
    public List<DeliveryReadyItemViewProj> getDeliveryReadyViewUnreleasedData() {
        return deliveryReadyItemRepository.findAllReleased(false);
    }

    // 5. -1 ?????? ??? ?????? ????????? ??????
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

    // 6. -1 ????????? ?????? ??????
    public void deleteOneDeliveryReadyViewData(UUID itemId) {
        deliveryReadyItemRepository.findByItemId(itemId).ifPresent(item -> {
            deliveryReadyItemRepository.delete(item);
        });
    }

    // 6. -2 ??????????????? ??????
    public void updateReleasedDeliveryReadyItem(UUID itemId) {
        deliveryReadyItemRepository.findByItemId(itemId).ifPresentOrElse(item -> {
            item.setReleased(false).setReleasedAt(null);

            deliveryReadyItemRepository.save(item);
        }, null);
    }

    // 7. -1 ???????????? ?????? ??????
    public List<DeliveryReadyItemOptionInfoProj> searchDeliveryReadyItemOptionInfo() {
        return deliveryReadyItemRepository.findAllOptionInfo();
    }

    // 7. -2 ?????? item ?????????????????? ??????
    public void updateDeliveryReadyItemOptionInfo(UUID itemId, String optionCode) {
        deliveryReadyItemRepository.findByItemId(itemId).ifPresentOrElse(item -> {
            if(optionCode.equals("null")) {
                item.setOptionManagementCode("");
            }
            else{
                item.setOptionManagementCode(optionCode);
            }
            deliveryReadyItemRepository.save(item);

        }, null);
    }

    // 7. -3 ?????? item ?????????????????? ?????? ??? ?????? ????????? ?????????????????? ?????? ??????
    public void updateDeliveryReadyItemsOptionInfo(UUID itemId, String optionCode) {
        deliveryReadyItemRepository.findByItemId(itemId).ifPresentOrElse(item -> {
            if(optionCode.equals("null")) {
                item.setOptionManagementCode("");
            }
            else{
                item.setOptionManagementCode(optionCode);
            }
            deliveryReadyItemRepository.save(item);

            // ?????? ????????? ????????? ?????? ??????
            updateDeliveryReadyItemChangedOption(item, optionCode);

        }, null);
    }

    // 7. -4 ?????? ????????? ?????????????????? ?????? ??????
    public void updateDeliveryReadyItemChangedOption(DeliveryReadyItemEntity item, String optionCode) {
        List<DeliveryReadyItemEntity> entities = deliveryReadyItemRepository.findByItems(item.getProdName(), item.getOptionInfo());

        for(DeliveryReadyItemEntity entity : entities) {
            if(optionCode.equals("null")) {
                entity.setOptionManagementCode("");
            }
            else{
                entity.setOptionManagementCode(optionCode);
            }
            deliveryReadyItemRepository.save(entity);
        }
    }

    // 9. -1 ???????????? ??? ??????????????? ????????? ??? ??? ?????? ??????
    public List<DeliveryReadyItemExcelFormDto> changeDeliveryReadyItem(List<DeliveryReadyItemViewDto> dtos) {
        List<DeliveryReadyItemEntity> entities = new ArrayList<>();

        // DeliveryReadyItemViewDto?????? DeliveryReadyItemEntity??? ????????????
        for(DeliveryReadyItemViewDto viewDto : dtos) {
            entities.add(viewDto.getDeliveryReadyItem());
        }

        // DeliveryReadyItemEntity??? DeliveryReadyItemExcelFromDto??? ?????????
        List<DeliveryReadyItemExcelFormDto> formDtos = getFromDtoByEntity(entities);

        return changeDuplicationEntity(formDtos);
    }

    // 9. -2 ???????????? ??? ???????????????????????? dto??? ??????
    public List<DeliveryReadyItemExcelFormDto> getFromDtoByEntity(List<DeliveryReadyItemEntity> entities) {
        List<DeliveryReadyItemExcelFormDto> formDtos = new ArrayList<>();

        for(DeliveryReadyItemEntity entity : entities){
            DeliveryReadyItemExcelFormDto formDto = new DeliveryReadyItemExcelFormDto();

            formDto.setProdOrderNumber(entity.getProdOrderNumber()).setOrderNumber(entity.getOrderNumber())
                    .setReceiver(entity.getReceiver()).setReceiverContact1(entity.getReceiverContact1())
                    .setZipCode(entity.getZipCode()).setDestination(entity.getDestination())
                    .setTransportNumber("").setProdName(entity.getProdName())
                    .setSender("????????????").setSenderContact1("070-0000-0000").setOptionInfo(entity.getOptionInfo())
                    .setOptionManagementCode(entity.getOptionManagementCode()).setUnit(entity.getUnit())
                    .setDeliveryMessage(entity.getDeliveryMessage()).setUnitA("").setAllProdOrderNumber(entity.getProdNumber()).setDuplication(false);

            formDtos.add(formDto);
        }

        return formDtos;
    }

    // 9. -3 (???????????? + ???????????? + ????????? + ????????????) ??????????????? ??????
    public List<DeliveryReadyItemExcelFormDto> changeDuplicationEntity(List<DeliveryReadyItemExcelFormDto> entities) {
        List<DeliveryReadyItemExcelFormDto> newOrderList = new ArrayList<>();

        // ???????????? > ???????????? > ????????? > ???????????? ??????
        entities.sort(Comparator.comparing(DeliveryReadyItemExcelFormDto::getOrderNumber)
                                .thenComparing(DeliveryReadyItemExcelFormDto::getReceiver)
                                .thenComparing(DeliveryReadyItemExcelFormDto::getProdName)
                                .thenComparing(DeliveryReadyItemExcelFormDto::getOptionInfo));

        Set<String> optionSet = new HashSet<>();        // ???????????? + ?????? + ????????? + ????????????

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
            int prevOrderIdx = newOrderList.size()-1;   // ???????????? ????????? ???????????? ????????? index

            // ???????????? + ?????? + ?????? : ????????? ??????
            if(!optionSet.add(receiverStr)){
                newOrderList.get(prevOrderIdx).setDuplication(true);
                entities.get(i).setDuplication(true);
            }

            // ???????????? + ?????? + ????????? + ???????????? : ????????? ??????
            if(!optionSet.add(resultStr)){
                DeliveryReadyItemExcelFormDto prevProd = newOrderList.get(prevOrderIdx);
                DeliveryReadyItemExcelFormDto currentProd = entities.get(i);

                newOrderList.get(prevOrderIdx).setUnit(prevProd.getUnit() + currentProd.getUnit());     // ?????????????????? ????????? ?????????
                newOrderList.get(prevOrderIdx).setAllProdOrderNumber(prevProd.getProdOrderNumber() + " / " + currentProd.getProdOrderNumber());     // ??? ???????????? ??????
            }else{
                newOrderList.add(entities.get(i));
            }
        }

        return newOrderList;
    }

    // 9. -4 ????????? ???????????? ??? ?????? ??????(released, released_at)
    @Transactional
    public void releasedDeliveryReadyItem(List<DeliveryReadyItemViewDto> dtos) {
        Date date = Calendar.getInstance().getTime();

        List<Integer> cidList = new ArrayList<>();
        
        for(DeliveryReadyItemViewDto dto : dtos){
            cidList.add(dto.getDeliveryReadyItem().getCid());
        }
        deliveryReadyItemRepository.updateReleasedAtByCid(cidList, date);
    }

    // public List<DeliveryReadyItemViewProj> getDeliveryReadyViewReleasedData(String currentDate) {
    //     List<DeliveryReadyItemViewProj> releasedItems = deliveryReadyItemRepository.findAllReleased(true);
    //     List<DeliveryReadyItemViewProj> todayReleasedItem = new ArrayList<>();

    //     for(DeliveryReadyItemViewProj releasedItem : releasedItems){
    //         Date storedReleasedAt = releasedItem.getDeliveryReadyItem().getReleasedAt();

    //         // UTC????????? KST??? ??????
    //         String releasedAt = changeLocalDate(storedReleasedAt);

    //         // ?????? ????????? ?????? ????????? ??????
    //         if(releasedAt.contains(currentDate)){
    //             todayReleasedItem.add(releasedItem);
    //         }
    //     }

    //     return todayReleasedItem;
    // }

    // public String changeLocalDate(Date storedReleasedAt) {
    //     LocalDateTime currentDate = LocalDateTime.ofInstant(storedReleasedAt.toInstant(), ZoneId.systemDefault());

    //     return currentDate.toString();
    // }
}