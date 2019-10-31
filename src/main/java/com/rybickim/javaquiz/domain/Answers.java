package com.rybickim.javaquiz.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Indexed;

import javax.persistence.*;
import java.util.List;

@Indexed
@Entity
@Data
@NoArgsConstructor
@Table(name = "answers")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        discriminatorType = DiscriminatorType.INTEGER,
        name = "answer_type_id",
        columnDefinition = "TINYINT(1)"
)
public class Answers implements Answerable {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Questions questions;

    @Override
    public String getCorrectValueString() {
        return "empty";
    }

    @Override
    public void addSentence(SentencesToChoose sentence) {

    }

    @Override
    public void removeSentence(SentencesToChoose sentence) {

    }

    @Override
    public void addSentences(List<SentencesToChoose> sentence) {

    }

    @Override
    public void removeSentences(List<SentencesToChoose> sentence) {

    }

    @Override
    public void addMissingWord(MissingWords missingWord) {

    }

    @Override
    public void removeMissingWord(MissingWords missingWord) {

    }

    @Override
    public void addMissingWords(List<MissingWords> words) {

    }

    @Override
    public void removeMissingWords(List<MissingWords> words) {

    }
}
