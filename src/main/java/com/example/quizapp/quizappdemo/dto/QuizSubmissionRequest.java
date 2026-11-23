package com.example.quizapp.quizappdemo.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class QuizSubmissionRequest {

    @NotNull(message = "userId zorunludur")
    private Long userId;

    @NotNull(message = "quizId zorunludur")
    private Long quizId;

    @NotNull(message = "Cevaplar zorunludur")
    private Map<Long, String> answers;

    public QuizSubmissionRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }
}
