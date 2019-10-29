package com.rybickim.javaquiz.service.impl;

import com.rybickim.javaquiz.data.AnswerCrudRepository;
import com.rybickim.javaquiz.data.QuestionCrudRepository;
import com.rybickim.javaquiz.domain.Answers;
import com.rybickim.javaquiz.domain.QuestionDTO;
import com.rybickim.javaquiz.service.AddQuizService;
import com.rybickim.javaquiz.service.AnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AddQuizServiceImpl implements AddQuizService {

    private static final Logger logger = LoggerFactory.getLogger(AddQuizServiceImpl.class);

    private AnswerCrudRepository answerCrudRepository;
    private QuestionCrudRepository questionCrudRepository;

    @Autowired
    public AddQuizServiceImpl(AnswerCrudRepository answerCrudRepository,
                              QuestionCrudRepository questionCrudRepository) {
        logger.debug("AnswerServiceImpl(): answerCrudRepository [{}], questionCrudRepository [{}]",
                answerCrudRepository,
                questionCrudRepository);
        this.answerCrudRepository = answerCrudRepository;
        this.questionCrudRepository = questionCrudRepository;
    }

    @Override
    public long writeFromDTO(QuestionDTO questionDTO) {
        // TODO
//        questionCrudRepository.save(question);

        return 0;
    }

    @Override
    public QuestionDTO readIntoDTO() {
        return null;
    }
}