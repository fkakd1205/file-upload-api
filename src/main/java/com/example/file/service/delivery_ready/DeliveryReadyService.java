package com.example.file.service.delivery_ready;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.file.model.delivery_ready.entity.DeliveryReadyFileEntity;
import com.example.file.model.delivery_ready.entity.DeliveryReadyItemEntity;
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

    public FileUploadResponse uploadDeliveryReadyExcelFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        bucket += "/naver-order";

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(file.getSize());

        s3Client.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), objMeta)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        // DeliveryReadyFileEntity 생성
        DeliveryReadyFileEntity fileEntity = createDeliveryReadyFileEntity(s3Client.getUrl(bucket, fileName).toString(), fileName, (int)file.getSize());
        createDeliveryReadyItemData(file, fileEntity);
                                                      
        return new FileUploadResponse(fileName, s3Client.getUrl(bucket, fileName).toString(), file.getContentType(), file.getSize());
    }

    public void isExcelFile(MultipartFile file) throws IOException{
        String extension = FilenameUtils.getExtension(file.getOriginalFilename().toLowerCase());

        if(EXTENSIONS_EXCEL.contains(extension)){
            return;
        }
        throw new IOException("엑셀파일만 업로드 해주세요.");
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
                .setOrderDateTime(row.getCell(56).getDateCellValue())
                .setReleased(false)
                .setDeliveryReadyFileCid(fileEntity.getCid());

            dataList.add(data);
        }

        deliveryReadyItemRepository.saveAll(dataList);
    }
}
