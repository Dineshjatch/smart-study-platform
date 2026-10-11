package com.smartstudy.quizservice.repository;

import com.smartstudy.quizservice.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository
        extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);

    List<QuizAttempt> findByQuizIdOrderByCompletedAtDesc(Long quizId);

    List<QuizAttempt> findByUserIdAndQuizIdOrderByCompletedAtDesc(
            Long userId,
            Long quizId
    );
}