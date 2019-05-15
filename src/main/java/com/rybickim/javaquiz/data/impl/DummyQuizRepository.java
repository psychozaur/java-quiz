package com.rybickim.javaquiz.data.impl;

import com.rybickim.javaquiz.data.QuizRepository;
import com.rybickim.javaquiz.domain.QuizExercise;
import com.rybickim.javaquiz.service.impl.DummyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
@Qualifier("dummyRepo")
public class DummyQuizRepository implements QuizRepository {

    private static final Logger logger = LoggerFactory.getLogger(DummyQuizRepository.class);

    private List<QuizExercise> quizExercises;

    public DummyQuizRepository() {
        this.quizExercises = Arrays.asList(new QuizExercise("What is your name?","Gallahad"),
                new QuizExercise("What is your quest?","To find the Holy Grail."),
                new QuizExercise("What's your favorite color?","Green! No, red! NOOOOOO!")
                );

        logger.debug("DummyQuizRepository(), quizExercises: {}", quizExercises);

        readAllQuizExercises();

    }

    @Override
    public List<QuizExercise> readAllQuizExercises() {

        logger.debug("readAllQuizExercises(), quizExercises: {}", quizExercises);

        return quizExercises;
    }
}
