package com.example.quizapp.quizappdemo.repository;

import com.example.quizapp.quizappdemo.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<QuizResult> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("delete from QuizResult r where r.user.id = :userId")
    int deleteByUserId(Long userId);
}
