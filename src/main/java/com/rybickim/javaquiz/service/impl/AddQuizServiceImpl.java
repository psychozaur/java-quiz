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
import java.util.stream.IntStream;
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

        Answers answer = new Answers();

        switch (answersEnum){
            case TRUE_FALSE:
                if (null != correctAnswer){
                    answer = new TrueFalseAnswers(Boolean.getBoolean(correctAnswer[0]));
                }
                break;
            case MULTIPLE_CHOICE:
                if (null != answersToChoose){
                    //first answer is default as the correct one
                    Integer correctOrdinal = IntStream.range(0, answersToChoose.length)
                            .filter(i -> answersToChoose[i] == correctAnswer[0])
                            .findFirst()
                            .orElse(0);
                    answer = new MultipleChoiceAnswers(correctOrdinal);
                    answer.addSentences(IntStream.range(0, answersToChoose.length)
                            .mapToObj(i -> new SentencesToChoose(i, answersToChoose[i]))
                            .collect(Collectors.toList()));
                }
                break;
            case MISSING_GAP:
                if (null != answerText && null != correctAnswer){
                    int highestLength = Integer.max(correctAnswer.length, answerText.length);
                    answer = new MissingGapAnswers();
                    answer.addMissingWords(IntStream.range(0, highestLength)
                            .mapToObj(i -> new MissingWords(i, answerText[i], correctAnswer[i]))
                            .collect(Collectors.toList()));
                }
                break;
                default:
                    answer = new Answers();
                    break;
        }

        newQuestion.addAnswer(answer);

        // TODO
        //  if explanation not null
        //  newQuestion.addExplanation

        newCategory.addQuestion(newQuestion);
        long newQuestionId = questionCrudRepository.save(newQuestion).getId();

        return newQuestionId;
    }

    @Override
    public QuestionDTO readIntoDTO() {
        return null;
    }
}