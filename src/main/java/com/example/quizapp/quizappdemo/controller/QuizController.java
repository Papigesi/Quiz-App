package com.example.quizapp.quizappdemo.controller;

import com.example.quizapp.quizappdemo.dto.QuestionDto;
import com.example.quizapp.quizappdemo.dto.QuizDto;
import com.example.quizapp.quizappdemo.service.QuestionService;
import com.example.quizapp.quizappdemo.service.QuizService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final QuestionService questionService;

    public QuizController(QuizService quizService, QuestionService questionService) {
        this.quizService = quizService;
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<QuizDto>> getAll() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getById(id));
    }

    /**
     * Bir quizin soruları — kullanıcı (doğru cevap gizli, sıralama rastgele).
     */
    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionDto>> getQuestionsForQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionsForQuiz(id));
    }

    @GetMapping("/{id}/questions/admin")
    public ResponseEntity<List<QuestionDto>> getQuestionsForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionsInQuizForAdmin(id));
    }


    @GetMapping("/{id}/available-questions")
    public ResponseEntity<List<QuestionDto>> getAvailableQuestions(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getAvailableQuestionsForQuiz(id));
    }

    @PostMapping
    public ResponseEntity<QuizDto> create(@Valid @RequestBody QuizDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuiz(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizDto> update(@PathVariable Long id, @Valid @RequestBody QuizDto dto) {
        return ResponseEntity.ok(quizService.updateQuiz(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok(Map.of("message", "Quiz başarıyla silindi"));
    }

    @PostMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<QuizDto> addQuestion(@PathVariable Long quizId, @PathVariable Long questionId) {
        return ResponseEntity.ok(quizService.addQuestionToQuiz(quizId, questionId));
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<QuizDto> removeQuestion(@PathVariable Long quizId, @PathVariable Long questionId) {
        return ResponseEntity.ok(quizService.removeQuestionFromQuiz(quizId, questionId));
    }
}
