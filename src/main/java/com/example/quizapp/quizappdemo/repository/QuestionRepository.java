package com.example.quizapp.quizappdemo.repository;

import com.example.quizapp.quizappdemo.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCategoryIdOrderByIdDesc(Long categoryId);

    List<Question> findAllByOrderByIdDesc();
}
