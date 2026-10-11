package com.smartstudy.quizservice.repository;

import com.smartstudy.quizservice.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizId(Long quizId);

    void deleteByQuizId(Long quizId);
}