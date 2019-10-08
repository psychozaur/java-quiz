package com.rybickim.javaquiz.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Indexed;

import javax.persistence.*;

@Indexed
@Entity
@Data
@NoArgsConstructor
@Table(name = "missing_words")
public class MissingWords {

    @Id
    @Column(name = "word_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Column(name = "mg_ordinal", nullable = false)
    Integer ordinal;

    String missingTerm;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "question_id")
    private Answers missingGapAnswers;

    public MissingWords(Integer ordinal, String missingTerm) {
        this.ordinal = ordinal;
        this.missingTerm = missingTerm;
    }
}
