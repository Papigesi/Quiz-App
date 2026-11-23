package com.example.quizapp.quizappdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Kategori adı boş olamaz")
    @Size(max = 80, message = "Kategori adı en fazla 80 karakter olabilir")
    private String categoryName;

    @Size(max = 300, message = "Açıklama en fazla 300 karakter olabilir")
    private String description;

    /** Sadece okuma (response) için doldurulur. */
    private Integer quizCount;

    public CategoryDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuizCount() {
        return quizCount;
    }

    public void setQuizCount(Integer quizCount) {
        this.quizCount = quizCount;
    }
}
