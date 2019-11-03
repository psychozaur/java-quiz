package com.rybickim.javaquiz.service;

import com.rybickim.javaquiz.domain.QuestionDTO;

public interface AddQuizService {

    long writeFromDTO(QuestionDTO questionDTO);

    QuestionDTO readIntoDTO();
}
