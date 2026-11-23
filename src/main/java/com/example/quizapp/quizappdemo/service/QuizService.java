package com.example.quizapp.quizappdemo.service;

import com.example.quizapp.quizappdemo.dto.QuizDto;
import com.example.quizapp.quizappdemo.exception.BadRequestException;
import com.example.quizapp.quizappdemo.exception.ResourceNotFoundException;
import com.example.quizapp.quizappdemo.model.Category;
import com.example.quizapp.quizappdemo.model.Question;
import com.example.quizapp.quizappdemo.model.Quiz;
import com.example.quizapp.quizappdemo.repository.QuestionRepository;
import com.example.quizapp.quizappdemo.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final int DEFAULT_TIME_LIMIT = 60;

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final CategoryService categoryService;

    public QuizService(QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       CategoryService categoryService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public List<QuizDto> getAllQuizzes() {
        return quizRepository.findAll()
                .stream()
                .map(this::toDto)
                .sorted((a, b) -> {
                    int c = a.getCategoryName().compareToIgnoreCase(b.getCategoryName());
                    return c != 0 ? c : a.getTitle().compareToIgnoreCase(b.getTitle());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public QuizDto createQuiz(QuizDto dto) {
        String title = dto.getTitle().trim();
        if (quizRepository.existsByTitle(title)) {
            throw new BadRequestException("Bu başlıkta bir quiz zaten var: " + title);
        }
        Category category = categoryService.findOrThrow(dto.getCategoryId());
        Quiz quiz = new Quiz(
                title,
                category,
                trimOrNull(dto.getDescription()),
                dto.getTimeLimitSeconds() != null ? dto.getTimeLimitSeconds() : DEFAULT_TIME_LIMIT
        );
        return toDto(quizRepository.save(quiz));
    }

    @Transactional
    public QuizDto updateQuiz(Long id, QuizDto dto) {
        Quiz quiz = findOrThrow(id);
        String newTitle = dto.getTitle().trim();
        if (!quiz.getTitle().equalsIgnoreCase(newTitle) && quizRepository.existsByTitle(newTitle)) {
            throw new BadRequestException("Bu başlıkta bir quiz zaten var: " + newTitle);
        }
        quiz.setTitle(newTitle);
        quiz.setDescription(trimOrNull(dto.getDescription()));
        if (dto.getTimeLimitSeconds() != null) {
            quiz.setTimeLimitSeconds(dto.getTimeLimitSeconds());
        }

        // Kategori değişti: bu quizdeki, yeni kategoriye uymayan soruları çıkar
        if (dto.getCategoryId() != null
                && !dto.getCategoryId().equals(quiz.getCategory().getId())) {
            Category newCategory = categoryService.findOrThrow(dto.getCategoryId());
            quiz.setCategory(newCategory);
            quiz.getQuestions().removeIf(q -> !q.getCategory().getId().equals(newCategory.getId()));
        }
        return toDto(quizRepository.save(quiz));
    }

    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = findOrThrow(id);
        // M:N: join tablo kayıtları silinir; sorular havuzda kalmaya devam eder.
        quiz.getQuestions().clear();
        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizDto addQuestionToQuiz(Long quizId, Long questionId) {
        Quiz quiz = findOrThrow(quizId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bu ID ile bir soru bulunamadı: " + questionId));

        if (!question.getCategory().getId().equals(quiz.getCategory().getId())) {
            throw new BadRequestException(
                    "Soru ve quiz aynı kategoride olmalı. " +
                            "Sorunun kategorisi: " + question.getCategory().getCategoryName() +
                            " — quizin kategorisi: " + quiz.getCategory().getCategoryName());
        }
        boolean added = quiz.getQuestions().add(question);
        if (!added) {
            throw new BadRequestException("Bu soru zaten bu quizde mevcut.");
        }
        return toDto(quizRepository.save(quiz));
    }

    @Transactional
    public QuizDto removeQuestionFromQuiz(Long quizId, Long questionId) {
        Quiz quiz = findOrThrow(quizId);
        boolean removed = quiz.getQuestions().removeIf(q -> q.getId().equals(questionId));
        if (!removed) {
            throw new BadRequestException("Bu soru bu quizde değil.");
        }
        return toDto(quizRepository.save(quiz));
    }

    public Quiz findOrThrow(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu ID ile bir quiz bulunamadı: " + id));
    }

    private QuizDto toDto(Quiz quiz) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        if (quiz.getCategory() != null) {
            dto.setCategoryId(quiz.getCategory().getId());
            dto.setCategoryName(quiz.getCategory().getCategoryName());
        }
        dto.setDescription(quiz.getDescription());
        dto.setTimeLimitSeconds(quiz.getTimeLimitSeconds());
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setQuestionCount(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0);
        return dto;
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
