package com.example.file.model.product.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "product")
@Accessors(chain = true)
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid")
    private Integer cid;

    @Type(type = "uuid-char")
    @Column(name = "id")
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "manufacturing_code")
    private String manufacturingCode;

    @Column(name = "naver_product_code")
    private String naverProductCode;

    @Column(name = "default_name")
    private String defaultName;

    @Column(name = "management_name")
    private String managementName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_file_name")
    private String imageFileName;

    @Column(name = "memo")
    private String memo;

    @Column(name = "created_at")
    private Date createdAt;

    @Type(type = "uuid-char")
    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Type(type = "uuid-char")
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "product_category_cid")
    private Integer productCategoryCid;
}
