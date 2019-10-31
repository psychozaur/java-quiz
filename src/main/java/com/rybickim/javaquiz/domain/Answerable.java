package com.rybickim.javaquiz.domain;

import java.util.List;

public interface Answerable {

    public String getCorrectValueString();

    public void addSentence(SentencesToChoose sentence);
    public void removeSentence(SentencesToChoose sentence);
    public void addSentences(List<SentencesToChoose> sentence);
    public void removeSentences(List<SentencesToChoose> sentence);

    public void addMissingWord(MissingWords missingWord);
    public void removeMissingWord(MissingWords missingWord);
    public void addMissingWords(List<MissingWords> words);
    public void removeMissingWords(List<MissingWords> words);

    }
