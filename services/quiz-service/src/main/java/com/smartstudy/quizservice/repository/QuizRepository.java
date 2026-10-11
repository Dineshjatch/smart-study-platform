package com.smartstudy.quizservice.repository;

import com.smartstudy.quizservice.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Quiz> findByIdAndUserId(Long id, Long userId);
}