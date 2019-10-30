package com.rybickim.javaquiz.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionDTO {

    private String question;
    private String category;
    private AnswersEnum answersEnum;
    private String[] correctAnswer;
    private String[] answerText;
    private String[] answersToChoose;
    private String explanation;
    private DBFile diagram;
}
