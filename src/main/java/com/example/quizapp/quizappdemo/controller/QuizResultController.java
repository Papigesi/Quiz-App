package com.example.quizapp.quizappdemo.controller;

import com.example.quizapp.quizappdemo.dto.QuizResultDto;
import com.example.quizapp.quizappdemo.dto.QuizSubmissionRequest;
import com.example.quizapp.quizappdemo.service.QuizResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/results")
public class QuizResultController {

    private final QuizResultService quizResultService;

    public QuizResultController(QuizResultService quizResultService) {
        this.quizResultService = quizResultService;
    }

    /**
     * Quiz bitiminde kullanıcının cevapları gönderilir; sunucu puanı hesaplar.
     */
    @PostMapping("/submit")
    public ResponseEntity<QuizResultDto> submit(@Valid @RequestBody QuizSubmissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizResultService.submitQuiz(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuizResultDto>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(quizResultService.getResultsByUserId(userId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, List<QuizResultDto>>> getLeaderboard() {
        return ResponseEntity.ok(quizResultService.getLeaderboardByCategory());
    }

    @GetMapping
    public ResponseEntity<List<QuizResultDto>> getAll() {
        return ResponseEntity.ok(quizResultService.getAllResults());
    }
}
