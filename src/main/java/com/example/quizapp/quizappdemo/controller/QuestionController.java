package com.example.quizapp.quizappdemo.controller;

import com.example.quizapp.quizappdemo.dto.QuestionDto;
import com.example.quizapp.quizappdemo.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Admin için: tüm sorular doğru cevaplarıyla birlikte.
     * categoryId verilirse o kategoriye filtrelenir.
     */
    @GetMapping("/admin")
    public ResponseEntity<List<QuestionDto>> getAllForAdmin(
            @RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return ResponseEntity.ok(questionService.getQuestionsByCategoryForAdmin(categoryId));
        }
        return ResponseEntity.ok(questionService.getAllQuestionsForAdmin());
    }

    @PostMapping
    public ResponseEntity<QuestionDto> create(@Valid @RequestBody QuestionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createQuestion(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDto> update(@PathVariable Long id, @Valid @RequestBody QuestionDto dto) {
        return ResponseEntity.ok(questionService.updateQuestion(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(Map.of("message", "Soru başarıyla silindi"));
    }
}
