package com.example.demo.dtos;



public class CategoryDto {
    private Long categoryId;
    private String code;
    private String nameTh;
    private String nameEn;

    public CategoryDto() {}

    public CategoryDto(Long categoryId, String code, String nameTh, String nameEn) {
        this.categoryId = categoryId;
        this.code = code;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameTh() {
        return nameTh;
    }

    public void setNameTh(String nameTh) {
        this.nameTh = nameTh;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }
}
