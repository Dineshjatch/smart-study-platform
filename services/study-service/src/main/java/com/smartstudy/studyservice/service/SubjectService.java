package com.smartstudy.studyservice.service;

import com.smartstudy.studyservice.dto.SubjectRequest;
import com.smartstudy.studyservice.dto.SubjectResponse;
import com.smartstudy.studyservice.model.Subject;
import com.smartstudy.studyservice.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public SubjectResponse createSubject(Long userId, SubjectRequest request) {

        Subject subject = new Subject(
                userId,
                request.getName(),
                request.getDescription()
        );

        Subject savedSubject = subjectRepository.save(subject);

        return toResponse(savedSubject);
    }

    public List<SubjectResponse> getSubjectsByUser(Long userId) {

        return subjectRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SubjectResponse getSubject(Long userId, Long subjectId) {

        Subject subject = findOwnedSubject(userId, subjectId);

        return toResponse(subject);
    }

    public SubjectResponse updateSubject(
            Long userId,
            Long subjectId,
            SubjectRequest request) {

        Subject subject = findOwnedSubject(userId, subjectId);

        subject.setName(request.getName());
        subject.setDescription(request.getDescription());

        Subject updatedSubject = subjectRepository.save(subject);

        return toResponse(updatedSubject);
    }

    public void deleteSubject(Long userId, Long subjectId) {

        Subject subject = findOwnedSubject(userId, subjectId);

        subjectRepository.delete(subject);
    }

    private Subject findOwnedSubject(Long userId, Long subjectId) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Subject not found"));

        // A user can only modify their own subjects.
        if (!subject.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Subject does not belong to this user");
        }

        return subject;
    }

    private SubjectResponse toResponse(Subject subject) {

        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDescription()
        );
    }
}