package com.rybickim.javaquiz.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Indexed;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.List;

@Indexed
@Entity
@Data
@NoArgsConstructor
@Table(name = "true_false_answers")
@DiscriminatorValue("1")
public class TrueFalseAnswers extends Answers {

    private static final List<String> values = Arrays.asList("true","false");

    private Boolean correctValue;

    public TrueFalseAnswers(Boolean correctValue){
        this.correctValue = correctValue;
    }

    @Override
    public String getCorrectValueString(){
        if (correctValue) return values.get(1);
        else return values.get(0);
    }
}
