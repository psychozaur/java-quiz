package com.rybickim.javaquiz.service;

import com.rybickim.javaquiz.domain.QuestionDTO;

public interface AddQuizService {

    //TODO
//    private String question;
//    private String category;
//    private String[] correctAnswer;
//    private String[] answerText;
//    private String[] answersToChoose;
//    private String explanation;
//    private DBFile diagram;

    // question and category fields are mandatory, right?

    // should we use Optional jic?

    /*
    1. writeFromDTO:
        findByQuestion
        findByCategory
            if found
                throw exception "Question of given name and category already exists"
            else
                addCategory
                if correctAnswer is not null
                    add correctAnswer
                if answerText is not null
                    add answerText
                if answersToChoose is not null
                    addAnswersToChoose
                if explanation is not null
                    add explanation
                    if diagram is not null
                        addDiagram
                saveQuestion
    2. readIntoDTO
     */

    long writeFromDTO(QuestionDTO questionDTO);

    QuestionDTO readIntoDTO();
}
