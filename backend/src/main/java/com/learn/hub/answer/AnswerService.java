package com.learn.hub.answer;

import com.learn.hub.content.CatalogStore;
import com.learn.hub.content.ContentLoader;
import com.learn.hub.content.ContentModels;
import com.learn.hub.interview.InterviewQuestionEntity;
import com.learn.hub.interview.InterviewQuestionRepository;
import com.learn.hub.quiz.QuizQuestionEntity;
import com.learn.hub.quiz.QuizQuestionRepository;
import com.learn.hub.topic.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 答题记录服务：判分（选择题）、保存自答、错题本与统计。
 */
@Service
public class AnswerService {

    public static final String SOURCE_TOPIC_QUIZ = "topic_quiz";
    public static final String SOURCE_INTERVIEW = "interview";

    private final AnswerRecordRepository answerRepository;
    private final InterviewQuestionRepository interviewRepository;
    private final QuizQuestionRepository quizRepository;
    private final ContentLoader contentLoader;
    private final CatalogStore catalogStore;

    public AnswerService(
            AnswerRecordRepository answerRepository,
            InterviewQuestionRepository interviewRepository,
            QuizQuestionRepository quizRepository,
            ContentLoader contentLoader,
            CatalogStore catalogStore
    ) {
        this.answerRepository = answerRepository;
        this.interviewRepository = interviewRepository;
        this.quizRepository = quizRepository;
        this.contentLoader = contentLoader;
        this.catalogStore = catalogStore;
    }

    /**
     * 提交一次作答：选择题由服务端判分，自答题按用户自评记录（可为空）。
     * 同一题重复作答只保留最近一次结果，并累加 attempts。
     */
    @Transactional
    public AnswerDtos.AnswerView submit(Long userId, AnswerDtos.SubmitRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String source = normalizeSource(request.source());
        if (request.questionId() == null) {
            throw new IllegalArgumentException("questionId 不能为空");
        }

        Question question = load(source, request.questionId());
        Integer choiceIndex = request.choiceIndex();
        String textAnswer = trim(request.textAnswer());
        Boolean correct;

        if ("choice".equals(question.type())) {
            if (choiceIndex == null || choiceIndex < 0 || choiceIndex >= question.options().size()) {
                throw new IllegalArgumentException("选择题必须提交有效的选项下标");
            }
            correct = choiceIndex == question.correctIndex();
            textAnswer = null;
        } else {
            if (textAnswer == null) {
                throw new IllegalArgumentException("自答题内容不能为空");
            }
            choiceIndex = null;
            correct = request.selfCorrect();
        }

        LocalDateTime now = LocalDateTime.now();
        AnswerRecordEntity entity = answerRepository
                .findByUserIdAndSourceAndQuestionId(userId, source, request.questionId())
                .orElse(null);
        if (entity == null) {
            entity = answerRepository.save(new AnswerRecordEntity(
                    userId, source, request.questionId(), question.type(),
                    choiceIndex, textAnswer, correct, now
            ));
        } else {
            entity.apply(question.type(), choiceIndex, textAnswer, correct, now);
            entity = answerRepository.save(entity);
        }
        return toView(entity, question);
    }

    /**
     * 答题记录 / 错题本：wrong=true 只看最近一次答错的题。
     * level 与 category 只对面试题库生效。
     */
    @Transactional(readOnly = true)
    public AnswerDtos.AnswerList list(
            Long userId,
            String source,
            Boolean wrong,
            String level,
            String category,
            int page,
            int size,
            String result
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 从 0 起，size 范围 1-100");
        }
        String sourceFilter = isBlank(source) ? null : normalizeSource(source);
        String levelFilter = isBlank(level) ? null : level.trim();
        String categoryFilter = isBlank(category) ? null : category.trim();
        String resultFilter = isBlank(result) ? null : result.trim().toLowerCase(Locale.ROOT);
        if (resultFilter != null
                && !List.of("all", "correct", "wrong", "ungraded").contains(resultFilter)) {
            throw new IllegalArgumentException("result 只能是 all / correct / wrong / ungraded");
        }

        List<AnswerDtos.AnswerView> views = new ArrayList<>();
        for (AnswerRecordEntity entity : answerRepository.findByUserIdOrderByUpdatedAtDesc(userId)) {
            if (sourceFilter != null && !sourceFilter.equals(entity.getSource())) {
                continue;
            }
            if (Boolean.TRUE.equals(wrong) && !Boolean.FALSE.equals(entity.getCorrect())) {
                continue;
            }
            if ("correct".equals(resultFilter) && !Boolean.TRUE.equals(entity.getCorrect())) {
                continue;
            }
            if ("wrong".equals(resultFilter) && !Boolean.FALSE.equals(entity.getCorrect())) {
                continue;
            }
            if ("ungraded".equals(resultFilter) && entity.getCorrect() != null) {
                continue;
            }
            Question question = tryLoad(entity.getSource(), entity.getQuestionId());
            if (question == null) {
                continue;
            }
            if (levelFilter != null && !levelFilter.equals(question.level())) {
                continue;
            }
            if (categoryFilter != null && !categoryFilter.equals(question.category())) {
                continue;
            }
            views.add(toView(entity, question));
        }

        int from = Math.min(page * size, views.size());
        int to = Math.min(from + size, views.size());
        return new AnswerDtos.AnswerList(views.subList(from, to), page, size, views.size());
    }

    /**
     * 统计：总数、答对、答错、未判定（自答未自评）。
     */
    @Transactional(readOnly = true)
    public AnswerDtos.Stats stats(Long userId) {
        List<AnswerRecordEntity> all = answerRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        long correct = all.stream().filter(item -> Boolean.TRUE.equals(item.getCorrect())).count();
        long wrong = all.stream().filter(item -> Boolean.FALSE.equals(item.getCorrect())).count();
        long ungraded = all.stream().filter(item -> item.getCorrect() == null).count();
        return new AnswerDtos.Stats(all.size(), correct, wrong, ungraded);
    }

    /**
     * 移出错题本：删除该题的作答记录。
     */
    @Transactional
    public void delete(Long userId, String source, Long questionId) {
        String value = normalizeSource(source);
        answerRepository.findByUserIdAndSourceAndQuestionId(userId, value, questionId)
                .ifPresent(answerRepository::delete);
    }

    private AnswerDtos.AnswerView toView(AnswerRecordEntity entity, Question question) {
        return new AnswerDtos.AnswerView(
                entity.getSource(),
                entity.getQuestionId(),
                question.type(),
                question.stem(),
                question.level(),
                question.category(),
                question.topicSlug(),
                question.topicTitle(),
                question.options(),
                question.correctIndex(),
                entity.getChoiceIndex(),
                entity.getTextAnswer(),
                entity.getCorrect(),
                question.explanation(),
                question.referenceAnswer(),
                entity.getAttempts(),
                entity.getUpdatedAt()
        );
    }

    private Question load(String source, Long questionId) {
        Question question = tryLoad(source, questionId);
        if (question == null) {
            throw new NotFoundException("QUESTION_NOT_FOUND", "未找到题目: " + questionId);
        }
        return question;
    }

    private Question tryLoad(String source, Long questionId) {
        if (SOURCE_INTERVIEW.equals(source)) {
            return interviewRepository.findById(questionId)
                    .map(this::fromInterview)
                    .orElse(null);
        }
        if (SOURCE_TOPIC_QUIZ.equals(source)) {
            QuizQuestionEntity entity = quizRepository.findById(questionId).orElse(null);
            if (entity == null) {
                return null;
            }
            ContentModels.QuizQuestion quiz = contentLoader.parseQuiz(entity.getPayload());
            String title = catalogStore.get().topic(entity.getTopicSlug())
                    .map(ContentModels.TopicDocument::title)
                    .orElse(entity.getTopicSlug());
            return new Question(
                    quiz.type(),
                    quiz.stem(),
                    null,
                    null,
                    entity.getTopicSlug(),
                    title,
                    quiz.options(),
                    quiz.correctIndex(),
                    quiz.referenceAnswer(),
                    quiz.explanation()
            );
        }
        return null;
    }

    private Question fromInterview(InterviewQuestionEntity entity) {
        return new Question(
                entity.getType(),
                entity.getStem(),
                entity.getLevel(),
                entity.getCategory(),
                null,
                null,
                contentLoader.parseOptions(entity.getOptions()),
                entity.getCorrectIndex(),
                entity.getReferenceAnswer(),
                entity.getExplanation()
        );
    }

    private String normalizeSource(String source) {
        String value = trim(source);
        if (!SOURCE_INTERVIEW.equals(value) && !SOURCE_TOPIC_QUIZ.equals(value)) {
            throw new IllegalArgumentException("source 只能是 interview 或 topic_quiz");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record Question(
            String type,
            String stem,
            String level,
            String category,
            String topicSlug,
            String topicTitle,
            List<String> options,
            Integer correctIndex,
            String referenceAnswer,
            String explanation
    ) {
    }
}
