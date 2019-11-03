package com.rybickim.javaquiz;

import com.rybickim.javaquiz.config.DatabaseConfig;
import com.rybickim.javaquiz.config.FileStorageConfig;
import com.rybickim.javaquiz.domain.*;
import com.rybickim.javaquiz.exception.FileStorageException;
import com.rybickim.javaquiz.service.*;
import com.rybickim.javaquiz.utils.IncorrectDifficultyException;
import com.rybickim.javaquiz.utils.QuizDifficulty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@Rollback(false)
@ContextConfiguration(classes = {JavaQuizApplication.class, DatabaseConfig.class, FileStorageConfig.class})
@WebAppConfiguration
public class JavaQuizDatabaseServicesTest {

    private static final Logger logger = LoggerFactory.getLogger(JavaQuizDatabaseServicesTest.class);

    private QuestionService questionService;
    private CategoryService categoryService;
    private ChosenQuestionService chosenQuestionService;
    private AnswerService answerService;
    private ExplanationService explanationService;
    private DBFileStorageService dbFileStorageService;
    private AddQuizService addQuizService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setCategoryService(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @Autowired
    public void setQuestionService(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Autowired
    public void setChosenQuestionService(ChosenQuestionService chosenQuestionService) {
        this.chosenQuestionService = chosenQuestionService;
    }

    @Autowired
    public void setAnswerService(AnswerService answerService) {
        this.answerService = answerService;
    }

    @Autowired
    public void setExplanationService(ExplanationService explanationService) {
        this.explanationService = explanationService;
    }

    @Autowired
    public void setDbFileStorageService(DBFileStorageService dbFileStorageService) {
        this.dbFileStorageService = dbFileStorageService;
    }

    @Autowired
    public void setAddQuizService(AddQuizService addQuizService) {
        this.addQuizService = addQuizService;
    }

    @Test
    public void testIfQuestionIsSaved(){


        // Given
        long questionsCount = questionService.countQuestions();
        Questions question = createQuestion(questionsCount + 1);

        // When
        questionService.saveQuestion(question);

        // Then
        assertEquals(questionsCount + 1, questionService.countQuestions());
    }

    @Transactional
    @Test
    public void testIfQuestionIsSavedViaEntityManager(){


        // Given
        long questionsCount = questionService.countQuestions();
        Questions question = createQuestion(questionsCount + 1);

        // When
        entityManager.persist(question);

        // Then
        assertEquals(questionsCount + 1, questionService.countQuestions());
    }

    @Transactional
    @Test
    public void testIfQuestionIsUpdatedWithFirstCategory(){

        // Given
        long questionsCount = questionService.countQuestions();
        Questions question = createQuestion(questionsCount + 1);
        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);

        // persist transient bare question before update
        question = questionService.saveQuestion(question);

        // When
        question.setCategories(firstCategory);

        // Then
        assertEquals(questionsCount + 1, questionService.countQuestions());
    }

    @Test
    public void testIfQuestionWithCategoryIsSaved(){

        // Given
        long questionsCount = questionService.countQuestions();
        long categoriesCount = categoryService.countCategories();
        Questions question = createQuestionWithCategory(questionsCount + 1, categoriesCount + 1);

        // When
        questionService.saveQuestion(question);

        // Then
        assertEquals(questionsCount + 1, questionService.countQuestions());
    }

    @Test
    public void testIfQuestionWithCategoryAndMultipleChoiceAnswerIsSaved(){


        // Given
        long questionsCount = questionService.countQuestions();
        long categoriesCount = categoryService.countCategories();
        Questions question = createQuestionWithCategoryAndMultipleChoiceAnswer(questionsCount + 1, categoriesCount + 1);

        // When
        questionService.saveQuestion(question);

        // Then
        assertEquals(questionsCount + 1, questionService.countQuestions());
    }

    @Test
    public void testIfQuestionIsNotFound(){
        // Given
        long nonExistingId = 1223232343434L;

        // When
        Optional<Questions> maybeQuestion = questionService.findQuestionById(nonExistingId);

        // Then
        assertEquals(Optional.empty(), maybeQuestion);
    }

    @Test
    public void testIfQuestionIsFound(){
        // Given
        long questionsCount = questionService.countQuestions();
        long categoriesCount = categoryService.countCategories();
        Questions question = createQuestionWithCategoryAndTrueFalseAnswer(questionsCount + 1, categoriesCount + 1);
        isQuestionNew(question);
        Long savedQuizId = questionService.saveQuestion(question).getId();

        // When
        Questions maybeQuestion = questionService.findQuestionById(savedQuizId).orElse(new Questions());
        isQuestionNew(maybeQuestion);

        // Then
        assertNotNull(maybeQuestion);
        assertEquals(question, maybeQuestion);
    }

    @Test
    public void testIfQuestionIsDeleted(){

        // Given
        long questionsCount = questionService.countQuestions();
        Questions question = createQuestion(questionsCount + 1);

        Long savedQuizId = questionService.saveQuestion(question).getId();


        assertEquals(questionsCount + 1, questionService.countQuestions());

        Optional<Questions> maybeQuestion = questionService.findQuestionById(savedQuizId);

        assertNotNull(maybeQuestion);
        assertNotEquals(Optional.empty(), maybeQuestion);
        // When

        questionService.deleteQuestionById(savedQuizId);
        maybeQuestion = questionService.findQuestionById(savedQuizId);

        // Then
        assertEquals(Optional.empty(), maybeQuestion);
    }

    // and now to make a test if the category isn't deleted with cascade...

    @Test
    public void testIfCategoryIsNotDeletedWhenQuestionIs(){
        // Given
        long questionsCount = questionService.countQuestions();
        long categoriesCount = categoryService.countCategories();
        Questions question = createQuestionWithCategoryAndMultipleChoiceAnswer(questionsCount + 1, categoriesCount + 1);
        Long savedQuizId = questionService.saveQuestion(question).getId();
        long savedCategoryId = savedQuizId + 1L;

        Optional<Categories> maybeCategory = categoryService.findCategoryById(savedCategoryId);

        assertNotEquals(Optional.empty(), maybeCategory);

        // When
        questionService.deleteQuestionById(savedQuizId);
        maybeCategory = categoryService.findCategoryById(savedCategoryId);

        // Then

        assertNotEquals(Optional.empty(), maybeCategory);
    }

    @Test
    public void testIfCategoryIsSaved(){

        // Given
        long categoriesCount = categoryService.countCategories();
        Categories category = createCategory(categoriesCount + 1);
        isCategoryNew(category);

        // When
        categoryService.saveCategory(category);

        // Then
        assertEquals(categoriesCount + 1, categoryService.countCategories());
    }

    @Transactional
    @Test
    public void testIfFoundCategoryNameCanBeUpdated(){

        // Given
        long questionsCount = questionService.countQuestions();
        long categoriesCount = categoryService.countCategories();
        Categories category = createCategoryWithQuestion(categoriesCount + 1, questionsCount + 1);
        entityManager.persist(category);
        entityManager.flush();

        assertNotEquals(0, categoryService.countCategories());

        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);
        isCategoryNew(firstCategory);

        // When
        firstCategory.setCategoryName("Zerg_" + (categoriesCount + 1));
        logger.debug("Name changed");
        isCategoryNew(firstCategory);
        logger.debug("Name in the first category is: " + firstCategory.getCategoryName());

        // Then
        assertEquals(categoriesCount + 1, categoryService.countCategories());

    }

    @Transactional
    @Test
    public void testIfFoundCategoryQuestionsCanBeUpdated(){

        // Given
        long questionsCount = questionService.countQuestions();
        long categoriesCount = categoryService.countCategories();
        Categories category = createCategoryWithQuestion(categoriesCount + 1, questionsCount + 1);
        entityManager.persist(category);
        entityManager.flush();

        assertNotEquals(0, categoryService.countCategories());

        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);
        Questions firstQuestion = questionService.findFirstByQuestion("Question_1");

        // When
        firstCategory.addQuestion(firstQuestion);

        // Then
        assertEquals(categoriesCount + 1, categoryService.countCategories());

    }

    @Test
    public void testIfCategoryWithQuestionIsSaved(){

        // Given
        long questionsCount = questionService.countQuestions();
        long categoriesCount = categoryService.countCategories();
        Categories category = createCategoryWithQuestion(categoriesCount + 1, questionsCount + 1);

        // When
        categoryService.saveCategory(category);

        // Then
        assertEquals(categoriesCount + 1, categoryService.countCategories());
    }

    @Test
    public void testIfCountEqualsListSize(){
        // Given
        int questionsInDb = questionService.listQuestions().size();

        // When
        int questionsCount = (int) questionService.countQuestions();

        // Then
        assertEquals(questionsCount, questionsInDb);
    }

    @Transactional
    @Test
    public void testIfUnassignedQuestionsAreSetToFirstCategory(){
        // Given
        long questionsCount = questionService.countQuestions();
        Questions unassignedQuestion = createQuestion(questionsCount + 1);

        entityManager.persist(unassignedQuestion);

        assertEquals(questionsCount + 1, questionService.countQuestions());

        // When
        List<Questions> searchResult = questionService.findQuestionsWithNullCategory();

        assertNotEquals(Collections.EMPTY_LIST, searchResult);

        assertNotNull(searchResult.get(0).getId());

        List<Categories> categoriesSearch = categoryService.findFirstByCategory(PageRequest.of(0,1));

        assertNotEquals(Collections.EMPTY_LIST, categoriesSearch);
        assertEquals(1, categoriesSearch.size());

        for (Questions question : searchResult){
            categoriesSearch.get(0).addQuestion(question);
        }

        // Then
        searchResult = questionService.findQuestionsWithNullCategory();

        assertEquals(Collections.EMPTY_LIST, searchResult);

    }

    @Test
    public void testIfCountQuestionsByCategoryWorks(){
        // Given
        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);

        assertNotNull(firstCategory);

        // When
        long result = questionService.countQuestionsByCategory(firstCategory);

        // Then
        assertEquals(result, questionService.countQuestionsByCategory(firstCategory));
    }

    @Test
    public void testIfFindQuestionsWithCategoryWorks(){
        // Given
        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);

        assertNotNull(firstCategory);

        // When
        List<Questions> resultList = questionService.findQuestionsWithCategory(firstCategory, PageRequest.of(0,3))
                .get()
                .collect(Collectors.toList());

        // Then
        assertEquals(questionService.countQuestionsByCategory(firstCategory), resultList.size());
    }

    // TODO thorough shuffle test (test selected questions as well)
    //  maybe with a diehard test for statistical integrity?
    //  or sort the two lists (first one: all questions from category, second one: after shuffling) and compare their contents?
    @Transactional
    @Test
    public void testIfListIsShuffled(){
        // Given
        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);

        assertNotNull(firstCategory);

        int questionsCount = (int) questionService.countQuestionsByCategory(firstCategory);

        // When
        List<Questions> resultList = questionService.getShuffledAllQuestions(firstCategory);

        // Then
        assertEquals(questionsCount, resultList.size());


    }

    @Test
    public void testIfGetAvailableDifficultiesWorks(){
        // Given
        int size = 20;

        // When
        Set<QuizDifficulty> difficulties = questionService.getAvailableDifficulties(size);

        // Then
        assertNotNull(difficulties);
        assertEquals(2, difficulties.size());
        assertTrue(difficulties.contains(QuizDifficulty.EASY));
        assertTrue(difficulties.contains(QuizDifficulty.MEDIUM));
//        assertTrue(difficulties.contains(QuizDifficulty.HARD));
    }

    @Transactional
    @Test
    public void testIfSelectedQuestionsAreShuffled(){
        // Given
        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);

        assertNotNull(firstCategory);

        // When
        List<Questions> resultList = null;
        try {
            resultList = questionService.getShuffledSelectedQuestions( QuizDifficulty.EASY, firstCategory);
        } catch (IncorrectDifficultyException e) {
            e.printStackTrace();
        }

        // Then
        assertNotNull(resultList);
        assertEquals(QuizDifficulty.EASY.noOfQuestions, resultList.size());
    }

    @Transactional
    @Test(expected = IncorrectDifficultyException.class)
    public void testIfIncorrectDifficultyExceptionIsThrown() throws IncorrectDifficultyException{
        // Given
        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);

        assertNotNull(firstCategory);

        // When
        questionService.getShuffledSelectedQuestions( QuizDifficulty.HARD, firstCategory);

    }

    @Transactional
    @Test
    public void testIfCreatingSingleChosenQuestionWorks(){
        // Given
        long questionsInDb = questionService.countQuestions();
        Questions newQuestion = createQuestion(questionsInDb + 1);
        questionService.saveQuestion(newQuestion);
        long questionId = newQuestion.getId();

        // When
        ChosenQuestions chosenQuestion = createOrFindChosenQuestion(newQuestion.getId());
        chosenQuestionService.saveChosenQuestion(chosenQuestion);
        long chosenQuestionId = chosenQuestion.getQuestions().getId();

        // Then
        assertNotNull(chosenQuestion);
        assertEquals(chosenQuestionId, questionId);

    }

    @Transactional
    @Test
    public void testIfRemovingSingleChosenQuestionWorks(){
        // Given
        long questionsChosenInDb = chosenQuestionService.countChosenQuestions();
        long chosenQuestionId = chosenQuestionService.findFirstChosenQuestions(PageRequest.of(0,1)).get(0).getQuestions().getId();

        // When
        removeChosenQuestion(chosenQuestionId);

        // Then
        assertEquals(questionsChosenInDb - 1, chosenQuestionService.countChosenQuestions());

    }

    @Transactional
    @Test
    public void testIfCreating5ChosenQuestionsWorks(){
        // Given
        long questionsChosenInDb = chosenQuestionService.countChosenQuestions();
        List<Questions> first5Questions = questionService.findFirst5ByOrderByIdAsc();

        Iterable<Long> questionIds = first5Questions.stream()
                .map(Questions::getId)
                .collect(Collectors.toList());

        // When
        for (Questions question : first5Questions){
            createOrFindChosenQuestion(question.getId());
        }

        Iterable<Long> chosenQuestionIds = convertIterableToList(chosenQuestionService.findAllChosenQuestionsById(questionIds)).stream()
                .map(ChosenQuestions::getQuestions)
                .map(Questions::getId)
                .collect(Collectors.toList());

        // Then
        assertNotNull(first5Questions);
        assertEquals(chosenQuestionIds, questionIds);

    }

    @Transactional
    @Test
    public void testIfCreatingAllChosenQuestionsFromCategoryWorks(){
        // Given
        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);
        List<Questions> questionsFromFirstCategory = questionService.findQuestionsWithCategory(firstCategory, PageRequest.of(0,10)).getContent();

        Iterable<Long> questionIds = questionsFromFirstCategory.stream()
                .map(Questions::getId)
                .collect(Collectors.toList());

        // When
        Iterable<ChosenQuestions> chosenQuestions = createOrFindMultipleChosenQuestions(questionIds);

        Iterable<Long> chosenQuestionIds = convertIterableToList(chosenQuestions).stream()
                .map(chosenQuestion -> chosenQuestion.getQuestions().getId())
                .collect(Collectors.toList());

        // Then
        assertNotNull(questionsFromFirstCategory);
        assertEquals(chosenQuestionIds, questionIds);

    }

    @Transactional
    @Test
    public void testIfRemovingAllChosenQuestionsFromCategoryWorks(){
        // Given
        long chosenQuestionsInDb = chosenQuestionService.countChosenQuestions();
        Categories firstCategory = categoryService.findFirstByCategory(PageRequest.of(0,1)).get(0);
        List<Questions> questionsFromFirstCategory = questionService.findQuestionsWithCategory(firstCategory, PageRequest.of(0,10)).getContent();

        Iterable<Long> chosenQuestionIds = questionsFromFirstCategory.stream()
                .map(Questions::getId)
                .collect(Collectors.toList());

        // When
        long removedChosenQuestions = removeMultipleChosenQuestions(chosenQuestionIds);

        // Then
        assertEquals(chosenQuestionsInDb - removedChosenQuestions, chosenQuestionService.countChosenQuestions());

    }

    @Transactional
    @Test
    public void testIfDeletingAllChosenQuestionsWorks(){
        // Given

        // When
        chosenQuestionService.deleteAllChosenQuestions();

        // Then
        assertEquals(0, chosenQuestionService.countChosenQuestions());

    }

    @Transactional
    @Test
    public void testIfImageIsStored(){
        // Given
        String originalFileName = "concurrenthashmap.png";
//        linux
//        String pathString = "/home/marcin/IdeaProjects/java-quiz/src/main/resources/static/img/concurrenthashmap.png";
//        windows
        String pathString = "C:\\Users\\laboratorium\\IdeaProjects\\java-quiz\\src\\main\\resources\\static\\img\\" + originalFileName;

        Path path = Paths.get(pathString);
        String name = originalFileName;
        String contentType = MediaType.IMAGE_PNG_VALUE;
        byte[] content = new byte[0];
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MultipartFile file = new MockMultipartFile(name, originalFileName, contentType, content);

        // When
        DBFile dbFile = dbFileStorageService.storeFile(file);
        String fileId = dbFile.getId();

        // Then
        assertNotNull(dbFile);
        assertEquals(dbFile, dbFileStorageService.getFile(fileId));
    }

    @Transactional
    @Test(expected = FileStorageException.class)
    public void testIfExceptionIsThrownWhenTxtFileIsStored(){
        // Given
        String originalFileName = "dummyTxtFile.txt";
//        linux
//        String pathString = "/home/marcin/IdeaProjects/java-quiz/src/main/resources/static/img/concurrenthashmap.png";
//        windows
        String pathString = "C:\\Users\\laboratorium\\IdeaProjects\\java-quiz\\src\\main\\resources\\static\\img\\" + originalFileName;

        Path path = Paths.get(pathString);
        String name = originalFileName;
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        byte[] content = new byte[0];
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MultipartFile file = new MockMultipartFile(name, originalFileName, contentType, content);

        // When
        DBFile dbFile = dbFileStorageService.storeFile(file);
        String fileId = dbFile.getId();

        // Then
        assertNotNull(dbFile);
        assertEquals(dbFile, dbFileStorageService.getFile(fileId));
    }

    @Transactional
    @Test
    public void testIfExplanationIsSaved(){
        // Given
        long firstQuestionId = questionService.findFirstQuestions(PageRequest.of(0,1)).get(0).getId();
        // When
        Explanations explanation = createOrFindExplanation(firstQuestionId);
        long explanationId = explanation.getQuestions().getId();
        // Then
        assertEquals(explanationId, firstQuestionId);
    }

    @Transactional
    @Test
    public void testIfExplanationTextIsSet(){
        // Given
        String longString = "Well, nothing is wrong; it just depends upon how you use it. For example, if you initialize a HashMap by just one thread and all threads are only reading from it, then it’s perfectly fine.\n" +
                "\n" +
                "One example of this is a Map that contains configuration properties. The real problem starts when at least one thread is updating the HashMap, i.e. adding, changing, or removing any key-value pair.\n" +
                "\n" +
                "Since the put() operation can cause re-sizing and lead to an infinite loop, that’s why either you should use Hashtable or ConcurrentHashMap, later is even better.";
        long firstQuestionId = questionService.findFirstQuestions(PageRequest.of(0,1)).get(0).getId();
        // When
        Explanations explanation = createOrFindExplanation(firstQuestionId);
        explanation.setExplanationText(longString);
        // Then
        assertEquals(longString, explanation.getExplanationText());
    }

    @Transactional
    @Test
    public void testIfExplanationDiagramIsSet(){

        // Given
        long questionsCount = questionService.countQuestions();
        Questions question = createQuestion(questionsCount + 1);
        questionService.saveQuestion(question);

        assertEquals(questionsCount + 1, questionService.countQuestions());

        long firstQuestionId = questionService.findFirstQuestions(PageRequest.of(0,1)).get(0).getId();

        String originalFileName = "concurrenthashmap.png";
//        linux
//        String pathString = "/home/marcin/IdeaProjects/java-quiz/src/main/resources/static/img/concurrenthashmap.png";
//        windows
        String pathString = "C:\\Users\\laboratorium\\IdeaProjects\\java-quiz\\src\\main\\resources\\static\\img\\" + originalFileName;

        Path path = Paths.get(pathString);
        String name = originalFileName;
        String contentType = MediaType.IMAGE_PNG_VALUE;
        byte[] content = new byte[0];
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MultipartFile file = new MockMultipartFile(name, originalFileName, contentType, content);

        DBFile dbFile = dbFileStorageService.storeFile(file);
        String fileId = dbFile.getId();

        // When
        Explanations explanation = createOrFindExplanation(firstQuestionId);
        explanation.setExplanationDiagramFileId(fileId);

        // Then
        assertEquals(explanation.getExplanationDiagramFileId(), fileId);
    }

    @Test
    public void testIfQuestionWithCategoryAndMissingGapAnswerIsSaved(){

        // Given
        long questionsCount = questionService.countQuestions();
        long categoriesCount = categoryService.countCategories();
        Questions question = createQuestionWithCategoryAndMissingGapAnswer(questionsCount + 1, categoriesCount + 1);

        // When
        questionService.saveQuestion(question);

        // Then
        assertEquals(questionsCount + 1, questionService.countQuestions());
    }


    //TODO what is says on the tin
    @Test
    public void testIfDataIsWrittenFromDTOCorrectly(){

        //Given
        long questionsCount = questionService.countQuestions();
        long categoriesCount = categoryService.countCategories();
        long answersCount = answerService.countAnswers();
        long explanationsCount = explanationService.countExplanations();

//        QuestionDTO questionDTO = new QuestionDTO();

        //When
//        addQuizService.writeFromDTO(questionDTO);

        //Then
        assertEquals(questionsCount + 1, questionService.countQuestions());
        assertEquals(categoriesCount + 1, categoryService.countCategories());
        assertEquals(answersCount + 1, answerService.countAnswers());
        assertEquals(explanationsCount + 1, explanationService.countExplanations());

    }

    /////////////////////////////////////////////
    //helper methods
    ///////////////////////////////////////////////////////////////

    private Questions createQuestion(long no){
        Questions question = new Questions("Question_" + no);

        logger.debug("Question - question: [{}]",
                question.getQuestion());

        return question;
    }

    private Questions createQuestionWithCategory(long questionNo, long categoryNo){
        Categories category = new Categories("Category_" + categoryNo);

        Questions question = new Questions("Question_" + questionNo);

        category.addQuestion(question);

        logger.debug("Question - question: [{}], category_name: [{}]",
                question.getQuestion(),
                question.getCategories().getCategoryName());

        return question;
    }

    private Questions createQuestionWithCategoryAndTrueFalseAnswer(long questionNo, long categoryNo){
        Questions question = new Questions("Question_" + questionNo);

        Answers answer = new TrueFalseAnswers(Boolean.FALSE);
        question.addAnswer(answer);

        Categories category = new Categories("Category_" + categoryNo);
        category.addQuestion(question);

        logger.debug("Question - question: [{}], answer: [{}], category_name: [{}]",
                question.getQuestion(),
                question.getAnswers(),
                question.getCategories().getCategoryName());

        return question;
    }

    private Questions createQuestionWithCategoryAndMissingGapAnswer(long questionNo, long categoryNo){
        Questions question = new Questions("Question_" + questionNo);

        List<MissingWords> missingWords = new ArrayList<>();
        for (int i = 1; i <= 5; i++){
            missingWords.add(new MissingWords(i,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                    "Word_" + i));
        }

        MissingGapAnswers answer = new MissingGapAnswers();
        answer.addMissingWords(missingWords);

        question.addAnswer(answer);

        Categories category = new Categories("Category_" + categoryNo);
        category.addQuestion(question);

        logger.debug("Question - question: [{}], answer: [{}], category_name: [{}]",
                question.getQuestion(),
                question.getAnswers(),
                question.getCategories().getCategoryName());

        return question;
    }

    private Questions createQuestionWithCategoryAndMultipleChoiceAnswer(long questionNo, long categoryNo){
        Questions question = new Questions("Question_" + questionNo);

        List<SentencesToChoose> sentences = new ArrayList<>();
        for (int i = 1; i <= 3; i++){
            sentences.add(new SentencesToChoose(i, "Sentence_" + i));
        }

        MultipleChoiceAnswers answer = new MultipleChoiceAnswers(2);
        answer.addSentences(sentences);

        question.addAnswer(answer);

        Categories category = new Categories("Category_" + categoryNo);
        category.addQuestion(question);

        logger.debug("Question - question: [{}], answer: [{}], category_name: [{}]",
                question.getQuestion(),
                question.getAnswers(),
                question.getCategories().getCategoryName());

        return question;
    }

    private Categories createCategory(long no){
        Categories category = new Categories("Category_" + no);

        logger.debug("Category - category_name: [{}]",
                category.getCategoryName());

        return category;
    }

    private Categories createCategoryWithQuestion(long categoryNo, long questionNo){
        Categories category = new Categories("Category_" + categoryNo);

        Questions question = new Questions("Question_" + questionNo);
        category.addQuestion(question);

        logger.debug("Category - category_name: [{}], question: [{}]",
                category.getCategoryName(),
                question.getQuestion());

        return category;
    }

    private ChosenQuestions createOrFindChosenQuestion(long questionId){
        ChosenQuestions chosenQuestion = chosenQuestionService.findChosenQuestionById(questionId).orElse(new ChosenQuestions());

        questionService.findQuestionById(questionId).ifPresent(question -> question.addChosenQuestion(chosenQuestion));

        return chosenQuestion;
    }

    private boolean removeChosenQuestion(long chosenQuestionId){
        Optional<ChosenQuestions> maybeChosenQuestion = chosenQuestionService.findChosenQuestionById(chosenQuestionId);

        maybeChosenQuestion.ifPresent(cq -> {
            questionService.findQuestionById(chosenQuestionId).ifPresent(question -> question.removeChosenQuestion(cq));
            chosenQuestionService.deleteChosenQuestionById(chosenQuestionId);
        });

        return maybeChosenQuestion.isPresent();
    }

    private List<ChosenQuestions> createOrFindMultipleChosenQuestions(Iterable<Long> ids) {
        List<ChosenQuestions> chosenQuestions = new ArrayList<>();

        ids.forEach(id -> chosenQuestions.add(createOrFindChosenQuestion(id)));

        return chosenQuestions;
    }

    private long removeMultipleChosenQuestions(Iterable<Long> ids) {
        List<Boolean> resultList = new ArrayList<>();
        ids.forEach(chosenQuestionId -> resultList.add(removeChosenQuestion(chosenQuestionId)));
        return resultList.stream()
                .filter(isRemoved -> isRemoved.equals(true))
                .count();
    }

    private Explanations createOrFindExplanation(long questionId){
        Explanations explanation = explanationService.findExplanationById(questionId).orElse(new Explanations());

        questionService.findQuestionById(questionId).ifPresent(question -> question.addExplanation(explanation));

        return explanation;
    }

    private boolean isQuestionNew(Questions question){
        Long id = question.getId();
        Class<Long> idType = Long.class;

        if (!idType.isPrimitive()) {
            logger.debug("isQuestionNew: first if clause: [{}]", id == null);
            return id == null;
        }

        if (id instanceof Number) {
            logger.debug("isQuestionNew: second if clause: [{}]", ((Number) id).longValue() == 0L);
            return ((Number) id).longValue() == 0L;
        }

        throw new IllegalArgumentException(String.format("Unsupported primitive id type %s!", idType));
    }

    private boolean isCategoryNew(Categories category){
        Long id = category.getId();
        Class<Long> idType = Long.class;

        if (!idType.isPrimitive()) {
            logger.debug("isCategoryNew: first if clause: [{}]", id == null);
            return id == null;
        }

        if (id instanceof Number) {
            logger.debug("isCategoryNew: second if clause: [{}]", ((Number) id).longValue() == 0L);
            return ((Number) id).longValue() == 0L;
        }

        throw new IllegalArgumentException(String.format("Unsupported primitive id type %s!", idType));
    }

    private <T> List<T> convertIterableToList(Iterable<T> iterable){
        List<T> resultList = new ArrayList<>();

        iterable.forEach(resultList::add);

        return resultList;
    }

    private byte[] extractBytes (String imageName) throws IOException {
        // open image
        File imgPath = new File(imageName);
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

        return ( data.getData() );
    }
}
