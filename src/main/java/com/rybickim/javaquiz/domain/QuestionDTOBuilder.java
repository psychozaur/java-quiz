package com.rybickim.javaquiz.domain;

public class QuestionDTOBuilder {

    private String question;
    private String category;
    private AnswersEnum answersEnum;
    private String[] correctAnswer;
    private String[] answerText;
    private String[] answersToChoose;
    private String explanation;
    private DBFile diagram;

    public QuestionDTOBuilder setQuestion(String question){
        this.question = question;
        return this;
    }

    public QuestionDTOBuilder setCategory(String category){
        this.category = category;
        return this;
    }

    public void setAnswersEnum(AnswersEnum answersEnum) {
        this.answersEnum = answersEnum;
    }

    public QuestionDTOBuilder setCorrectAnswer(String[] correctAnswer){
        this.correctAnswer = correctAnswer;
        return this;
    }

    public QuestionDTOBuilder setAnswerText(String[] answerText){
        this.answerText = answerText;
        return this;
    }

    public QuestionDTOBuilder setAnswersToChoose(String[] answersToChoose){
        this.answersToChoose = answersToChoose;
        return this;
    }

    public QuestionDTOBuilder setExplanation(String explanation){
        this.explanation = explanation;
        return this;
    }

    public QuestionDTOBuilder setDiagram(DBFile diagram){
        this.diagram = diagram;
        return this;
    }

    public QuestionDTO buildQuestionDTO() {
        return new QuestionDTO(question,
                category,
                answersEnum,
                correctAnswer,
                answerText,
                answersToChoose,
                explanation,
                diagram);
    }
}
