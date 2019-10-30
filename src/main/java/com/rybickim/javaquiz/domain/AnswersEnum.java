package com.rybickim.javaquiz.domain;

public enum AnswersEnum {
    TRUE_FALSE(1),
    MULTIPLE_CHOICE(2),
    MISSING_GAP(3);

    private int dType;

    AnswersEnum(int dType) {
        this.dType = dType;
    }
}
