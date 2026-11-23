package com.example.quizapp.quizappdemo.service;

import com.example.quizapp.quizappdemo.dto.QuizResultDto;
import com.example.quizapp.quizappdemo.dto.QuizSubmissionRequest;
import com.example.quizapp.quizappdemo.exception.BadRequestException;
import com.example.quizapp.quizappdemo.model.Question;
import com.example.quizapp.quizappdemo.model.Quiz;
import com.example.quizapp.quizappdemo.model.QuizResult;
import com.example.quizapp.quizappdemo.model.User;
import com.example.quizapp.quizappdemo.repository.QuizResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizResultService {

    private final QuizResultRepository quizResultRepository;
    private final UserService userService;
    private final QuizService quizService;

    public QuizResultService(QuizResultRepository quizResultRepository,
                             UserService userService,
                             QuizService quizService) {
        this.quizResultRepository = quizResultRepository;
        this.userService = userService;
        this.quizService = quizService;
    }

    @Transactional
    public QuizResultDto submitQuiz(QuizSubmissionRequest request) {
        if (request.getAnswers() == null) {
            throw new BadRequestException("Cevaplar boş olamaz");
        }

        User user = userService.findUserOrThrow(request.getUserId());
        Quiz quiz = quizService.findOrThrow(request.getQuizId());
        Set<Question> questions = quiz.getQuestions();
        if (questions.isEmpty()) {
            throw new BadRequestException("Bu quizde henüz soru yok: " + quiz.getTitle());
        }

        int correct = 0;
        for (Question q : questions) {
            String userAnswer = request.getAnswers().get(q.getId());
            if (userAnswer != null && userAnswer.equalsIgnoreCase(q.getCorrectAnswer())) {
                correct++;
            }
        }
        int total = questions.size();
        int score = (int) Math.round((correct * 100.0) / total);

        QuizResult result = new QuizResult();
        result.setUser(user);
        result.setQuiz(quiz);
        result.setQuizTitle(quiz.getTitle());
        result.setCategory(quiz.getCategory() != null ? quiz.getCategory().getCategoryName() : null);
        result.setScore(score);
        result.setTotalQuestions(total);
        result.setCorrectAnswers(correct);

        return toDto(quizResultRepository.save(result));
    }

    @Transactional(readOnly = true)
    public List<QuizResultDto> getResultsByUserId(Long userId) {
        userService.findUserOrThrow(userId);
        return quizResultRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, List<QuizResultDto>> getLeaderboardByCategory() {
        Map<String, Map<Long, QuizResult>> bestByCategoryAndUser = new LinkedHashMap<>();

        for (QuizResult r : quizResultRepository.findAll()) {
            if (r.getUser() == null) continue;
            String category = effectiveCategory(r);
            if (category == null) continue;

            Map<Long, QuizResult> bucket = bestByCategoryAndUser
                    .computeIfAbsent(category, k -> new LinkedHashMap<>());
            QuizResult existing = bucket.get(r.getUser().getId());
            if (existing == null || isBetter(r, existing)) {
                bucket.put(r.getUser().getId(), r);
            }
        }

        Map<String, List<QuizResultDto>> result = new LinkedHashMap<>();
        bestByCategoryAndUser.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(entry -> {
                    List<QuizResultDto> sorted = entry.getValue().values().stream()
                            .sorted(Comparator
                                    .comparingInt(QuizResult::getScore).reversed()
                                    .thenComparing(QuizResult::getCreatedAt))
                            .map(this::toDto)
                            .collect(Collectors.toList());
                    result.put(entry.getKey(), sorted);
                });
        return result;
    }

    private boolean isBetter(QuizResult candidate, QuizResult current) {
        if (candidate.getScore() != current.getScore()) {
            return candidate.getScore() > current.getScore();
        }
        return candidate.getCreatedAt().isBefore(current.getCreatedAt());
    }

    private String effectiveCategory(QuizResult r) {
        if (r.getQuiz() != null && r.getQuiz().getCategory() != null) {
            return r.getQuiz().getCategory().getCategoryName();
        }
        return r.getCategory();
    }

    @Transactional(readOnly = true)
    public List<QuizResultDto> getAllResults() {
        return quizResultRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private QuizResultDto toDto(QuizResult r) {
        QuizResultDto dto = new QuizResultDto();
        dto.setId(r.getId());

        if (r.getUser() != null) {
            dto.setUserId(r.getUser().getId());
            dto.setUserName(r.getUser().getName());
        } else {
            dto.setUserName("Bilinmeyen Kullanıcı");
        }

        if (r.getQuiz() != null) {
            dto.setQuizId(r.getQuiz().getId());
            dto.setQuizTitle(r.getQuiz().getTitle());
            if (r.getQuiz().getCategory() != null) {
                dto.setCategory(r.getQuiz().getCategory().getCategoryName());
            } else {
                dto.setCategory(r.getCategory());
            }
        } else {
            dto.setQuizTitle(r.getQuizTitle() != null ? r.getQuizTitle() + " (silinmiş)" : "(silinmiş quiz)");
            dto.setCategory(r.getCategory());
        }

        dto.setScore(r.getScore());
        dto.setTotalQuestions(r.getTotalQuestions());
        dto.setCorrectAnswers(r.getCorrectAnswers());
        dto.setCreatedAt(r.getCreatedAt());
        
        return dto;
    }
}
