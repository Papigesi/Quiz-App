package com.example.quizapp.quizappdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDto {

    private Long id;

    @NotNull(message = "categoryId zorunludur")
    private Long categoryId;

    @NotBlank(message = "Soru metni boş olamaz")
    private String questionText;

    @NotBlank(message = "A şıkkı boş olamaz")
    private String optionA;

    @NotBlank(message = "B şıkkı boş olamaz")
    private String optionB;

    @NotBlank(message = "C şıkkı boş olamaz")
    private String optionC;

    @NotBlank(message = "D şıkkı boş olamaz")
    private String optionD;

    @NotBlank(message = "Doğru cevap belirtilmelidir")
    @Pattern(regexp = "^[ABCDabcd]$", message = "Doğru cevap A, B, C veya D olmalıdır")
    private String correctAnswer;

    private String categoryName;

    private Integer quizCount;

    public QuestionDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getQuizCount() {
        return quizCount;
    }

    public void setQuizCount(Integer quizCount) {
        this.quizCount = quizCount;
    }
}
