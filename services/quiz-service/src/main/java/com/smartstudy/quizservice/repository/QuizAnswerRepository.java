package com.smartstudy.quizservice.repository;

import com.smartstudy.quizservice.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAnswerRepository
        extends JpaRepository<QuizAnswer, Long> {

    List<QuizAnswer> findByAttemptId(Long attemptId);

    void deleteByAttemptId(Long attemptId);
}