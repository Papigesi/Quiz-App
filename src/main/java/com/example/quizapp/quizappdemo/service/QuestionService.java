package com.example.quizapp.quizappdemo.service;

import com.example.quizapp.quizappdemo.dto.QuestionDto;
import com.example.quizapp.quizappdemo.exception.BadRequestException;
import com.example.quizapp.quizappdemo.exception.ResourceNotFoundException;
import com.example.quizapp.quizappdemo.model.Category;
import com.example.quizapp.quizappdemo.model.Question;
import com.example.quizapp.quizappdemo.model.Quiz;
import com.example.quizapp.quizappdemo.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CategoryService categoryService;
    private final QuizService quizService;

    public QuestionService(QuestionRepository questionRepository,
                           CategoryService categoryService,
                           QuizService quizService) {
        this.questionRepository = questionRepository;
        this.categoryService = categoryService;
        this.quizService = quizService;
    }

    @Transactional
    public QuestionDto createQuestion(QuestionDto dto) {
        Category category = categoryService.findOrThrow(dto.getCategoryId());
        Question question = new Question(
                dto.getQuestionText().trim(),
                dto.getOptionA().trim(),
                dto.getOptionB().trim(),
                dto.getOptionC().trim(),
                dto.getOptionD().trim(),
                dto.getCorrectAnswer().toUpperCase(),
                category
        );
        return toDto(questionRepository.save(question), true);
    }

    @Transactional
    public QuestionDto updateQuestion(Long id, QuestionDto dto) {
        Question question = findOrThrow(id);

        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(question.getCategory().getId())) {
            Category newCategory = categoryService.findOrThrow(dto.getCategoryId());
            // Kategori değiştiğinde, bu soruyu farklı kategorideki quizlerden otomatik çıkar
            // (Quiz tarafında ManyToMany'nin sahip tarafı olduğu için onu temizliyoruz.)
            for (Quiz quiz : new ArrayList<>(question.getQuizzes())) {
                if (!quiz.getCategory().getId().equals(newCategory.getId())) {
                    quiz.getQuestions().remove(question);
                }
            }
            question.setCategory(newCategory);
        }

        question.setQuestionText(dto.getQuestionText().trim());
        question.setOptionA(dto.getOptionA().trim());
        question.setOptionB(dto.getOptionB().trim());
        question.setOptionC(dto.getOptionC().trim());
        question.setOptionD(dto.getOptionD().trim());
        question.setCorrectAnswer(dto.getCorrectAnswer().toUpperCase());
        return toDto(questionRepository.save(question), true);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = findOrThrow(id);
        // M:N: önce sahip tarafındaki tüm quiz->question bağlarını koparalım.
        for (Quiz quiz : new ArrayList<>(question.getQuizzes())) {
            quiz.getQuestions().remove(question);
        }
        questionRepository.delete(question);
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsForQuiz(Long quizId) {
        Quiz quiz = quizService.findOrThrow(quizId);
        List<Question> questions = new ArrayList<>(quiz.getQuestions());
        if (questions.isEmpty()) {
            throw new BadRequestException("Bu quizde henüz soru yok.");
        }
        Collections.shuffle(questions);
        return questions.stream()
                .map(q -> toDto(q, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getAllQuestionsForAdmin() {
        return questionRepository.findAllByOrderByIdDesc()
                .stream()
                .map(q -> toDto(q, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsByCategoryForAdmin(Long categoryId) {
        categoryService.findOrThrow(categoryId);
        return questionRepository.findByCategoryIdOrderByIdDesc(categoryId)
                .stream()
                .map(q -> toDto(q, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getAvailableQuestionsForQuiz(Long quizId) {
        Quiz quiz = quizService.findOrThrow(quizId);
        Long categoryId = quiz.getCategory().getId();
        return questionRepository.findByCategoryIdOrderByIdDesc(categoryId)
                .stream()
                .filter(q -> !quiz.getQuestions().contains(q))
                .map(q -> toDto(q, true))
                .collect(Collectors.toList());
    }

    /**
     * Admin için: bir quizin içindeki sorular (doğru cevap dahil),
     * eklendiği sıraya göre (Set'in iteration sırası = LinkedHashSet).
     */
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsInQuizForAdmin(Long quizId) {
        Quiz quiz = quizService.findOrThrow(quizId);
        return quiz.getQuestions().stream()
                .map(q -> toDto(q, true))
                .collect(Collectors.toList());
    }

    public Question findOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu ID ile bir soru bulunamadı: " + id));
    }

    QuestionDto toDto(Question q, boolean includeCorrectAnswer) {
        QuestionDto dto = new QuestionDto();
        dto.setId(q.getId());
        dto.setQuestionText(q.getQuestionText());
        dto.setOptionA(q.getOptionA());
        dto.setOptionB(q.getOptionB());
        dto.setOptionC(q.getOptionC());
        dto.setOptionD(q.getOptionD());
        if (q.getCategory() != null) {
            dto.setCategoryId(q.getCategory().getId());
            dto.setCategoryName(q.getCategory().getCategoryName());
        }
        dto.setQuizCount(q.getQuizzes() != null ? q.getQuizzes().size() : 0);
        if (includeCorrectAnswer) {
            dto.setCorrectAnswer(q.getCorrectAnswer());
        }
        return dto;
    }
}
