package com.smartstudy.studyservice.service;

import com.smartstudy.studyservice.dto.SubjectRequest;
import com.smartstudy.studyservice.dto.SubjectResponse;
import com.smartstudy.studyservice.model.Subject;
import com.smartstudy.studyservice.repository.SubjectRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubjectServiceTest {

    @Test
    void createSubjectShouldSaveAndReturnSubject() {

        SubjectRepository subjectRepository = mock(SubjectRepository.class);

        Subject subject = new Subject(
                1L,
                "Java",
                "Object-oriented programming"
        );

        when(subjectRepository.save(any(Subject.class)))
                .thenReturn(subject);

        SubjectService subjectService =
                new SubjectService(subjectRepository);

        SubjectRequest request = new SubjectRequest();
        request.setName("Java");
        request.setDescription("Object-oriented programming");

        SubjectResponse response =
                subjectService.createSubject(1L, request);

        assertEquals("Java", response.getName());
        assertEquals(
                "Object-oriented programming",
                response.getDescription()
        );

        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void getSubjectsByUserShouldReturnOnlyUsersSubjects() {

        SubjectRepository subjectRepository = mock(SubjectRepository.class);

        Subject java = new Subject(
                1L,
                "Java",
                "Programming"
        );

        Subject dbms = new Subject(
                1L,
                "DBMS",
                "Databases"
        );

        when(subjectRepository.findByUserId(1L))
                .thenReturn(List.of(java, dbms));

        SubjectService subjectService =
                new SubjectService(subjectRepository);

        List<SubjectResponse> subjects =
                subjectService.getSubjectsByUser(1L);

        assertEquals(2, subjects.size());
        assertEquals("Java", subjects.get(0).getName());
        assertEquals("DBMS", subjects.get(1).getName());

        verify(subjectRepository).findByUserId(1L);
    }

    @Test
    void getSubjectShouldReturnOwnedSubject() {

        SubjectRepository subjectRepository = mock(SubjectRepository.class);

        Subject subject = new Subject(
                1L,
                "Operating Systems",
                "Processes and memory"
        );

        when(subjectRepository.findById(10L))
                .thenReturn(Optional.of(subject));

        SubjectService subjectService =
                new SubjectService(subjectRepository);

        SubjectResponse response =
                subjectService.getSubject(1L, 10L);

        assertEquals("Operating Systems", response.getName());
        assertEquals(
                "Processes and memory",
                response.getDescription()
        );

        verify(subjectRepository).findById(10L);
    }

    @Test
    void updateSubjectShouldChangeOwnedSubject() {

        SubjectRepository subjectRepository = mock(SubjectRepository.class);

        Subject subject = new Subject(
                1L,
                "Java",
                "Basic Java"
        );

        when(subjectRepository.findById(10L))
                .thenReturn(Optional.of(subject));

        when(subjectRepository.save(subject))
                .thenReturn(subject);

        SubjectService subjectService =
                new SubjectService(subjectRepository);

        SubjectRequest request = new SubjectRequest();
        request.setName("Advanced Java");
        request.setDescription("Advanced Java concepts");

        SubjectResponse response =
                subjectService.updateSubject(1L, 10L, request);

        assertEquals("Advanced Java", response.getName());
        assertEquals(
                "Advanced Java concepts",
                response.getDescription()
        );

        verify(subjectRepository).save(subject);
    }

    @Test
    void deleteSubjectShouldDeleteOwnedSubject() {

        SubjectRepository subjectRepository = mock(SubjectRepository.class);

        Subject subject = new Subject(
                1L,
                "DBMS",
                "Database concepts"
        );

        when(subjectRepository.findById(10L))
                .thenReturn(Optional.of(subject));

        SubjectService subjectService =
                new SubjectService(subjectRepository);

        subjectService.deleteSubject(1L, 10L);

        verify(subjectRepository).delete(subject);
    }

    @Test
    void userShouldNotModifyAnotherUsersSubject() {

        SubjectRepository subjectRepository = mock(SubjectRepository.class);

        Subject subject = new Subject(
                2L,
                "Private Subject",
                "Belongs to another user"
        );

        when(subjectRepository.findById(10L))
                .thenReturn(Optional.of(subject));

        SubjectService subjectService =
                new SubjectService(subjectRepository);

        SubjectRequest request = new SubjectRequest();
        request.setName("Hacked Subject");
        request.setDescription("Should not be allowed");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subjectService.updateSubject(1L, 10L, request)
        );

        assertEquals(
                "Subject does not belong to this user",
                exception.getMessage()
        );

        verify(subjectRepository, never()).save(any(Subject.class));
    }
}