package com.rybickim.javaquiz.service.impl;

import com.rybickim.javaquiz.data.AnswerCrudRepository;
import com.rybickim.javaquiz.data.CategoryCrudRepository;
import com.rybickim.javaquiz.data.QuestionCrudRepository;
import com.rybickim.javaquiz.domain.*;
import com.rybickim.javaquiz.service.AddQuizService;
import com.rybickim.javaquiz.service.AnswerService;
import com.rybickim.javaquiz.utils.CategoryAlreadyExistsException;
import com.rybickim.javaquiz.utils.QuestionAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AddQuizServiceImpl implements AddQuizService {

    private static final Logger logger = LoggerFactory.getLogger(AddQuizServiceImpl.class);

    private AnswerCrudRepository answerCrudRepository;
    private CategoryCrudRepository categoryCrudRepository;
    private QuestionCrudRepository questionCrudRepository;

    @Autowired
    public AddQuizServiceImpl(AnswerCrudRepository answerCrudRepository,
                              CategoryCrudRepository categoryCrudRepository,
                              QuestionCrudRepository questionCrudRepository) {
        logger.debug("AnswerServiceImpl(): answerCrudRepository [{}], " +
                "categoryCrudRepository [{}]" +
                        "questionCrudRepository [{}]",
                answerCrudRepository,
                categoryCrudRepository,
                questionCrudRepository);
        this.answerCrudRepository = answerCrudRepository;
        this.categoryCrudRepository = categoryCrudRepository;
        this.questionCrudRepository = questionCrudRepository;
    }

    @Override
    public long writeFromDTO(QuestionDTO questionDTO) {

        Optional<Questions> maybeQuestion = Optional.of(questionCrudRepository.findFirstByQuestion(questionDTO.getQuestion()));
        Optional<Categories> maybeCategory = Optional.of(categoryCrudRepository.findFirstByCategoryName(questionDTO.getCategory()));

        maybeQuestion.ifPresent(question -> {
            throw new QuestionAlreadyExistsException(question.getQuestion());
        });

        Questions newQuestion = new Questions(questionDTO.getQuestion());

        maybeCategory.ifPresent(category -> {
            throw new CategoryAlreadyExistsException(category.getCategoryName());
        });

        Categories newCategory = new Categories(questionDTO.getCategory());

        AnswersEnum answersEnum = questionDTO.getAnswersEnum();
        String[] correctAnswer = questionDTO.getCorrectAnswer();
        String[] answerText = questionDTO.getAnswerText();
        String[] answersToChoose = questionDTO.getAnswersToChoose();
        String explanation = questionDTO.getExplanation();
        DBFile dbFile = questionDTO.getDiagram();

        Answers answer;

        switch (answersEnum){
            case TRUE_FALSE:
                answer = new TrueFalseAnswers(Boolean.getBoolean(questionDTO.getCorrectAnswer()[0]));
                break;
            case MULTIPLE_CHOICE:
                answer = new MultipleChoiceAnswers();
                break;
            case MISSING_GAP:
                answer = new MissingGapAnswers();
                break;
                default:
                    answer = new Answers();
                    break;
        }

        if (null != correctAnswer) {
            // how not to break polymorphism
//            answer.addSentences((Arrays.stream(questionDTO.getAnswersToChoose())
//                    .map(answerToChoose -> new SentencesToChoose(,answerToChoose))
//                    .collect(Collectors.toList())));
//            answer.addMissingWords(questionDTO.getAnswerText());
        }

        // TODO
        //        newCategory.addQuestion(newQuestion);
        //        questionCrudRepository.save(newQuestion);

        return 0;
    }

    @Override
    public QuestionDTO readIntoDTO() {
        return null;
    }
}