package com.example.quizapp.quizappdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizDto {

    private Long id;

    @NotBlank(message = "Başlık boş olamaz")
    @Size(max = 120, message = "Başlık en fazla 120 karakter olabilir")
    private String title;

    @NotNull(message = "categoryId zorunludur (sadece var olan kategorilerden seçilebilir)")
    private Long categoryId;

    private String categoryName;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
    private String description;

    @Min(value = 10, message = "Süre en az 10 saniye olmalıdır")
    private Integer timeLimitSeconds;

    private LocalDateTime createdAt;

    private Integer questionCount;

    public QuizDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
}
