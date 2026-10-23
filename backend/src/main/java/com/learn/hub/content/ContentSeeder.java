package com.learn.hub.content;

import com.learn.hub.interview.InterviewDtos;
import com.learn.hub.interview.InterviewService;
import com.learn.hub.persistence.TopicEntity;
import com.learn.hub.persistence.TopicRepository;
import com.learn.hub.persistence.TrackEntity;
import com.learn.hub.persistence.TrackRepository;
import com.learn.hub.quiz.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容种子同步器：content/seed-version.txt 变化时，把学习项、路线、随堂题与面试题
 * 增量同步到数据库。版本未变化时不动数据库，因此管理端的日常编辑不会被覆盖。
 */
@Component
public class ContentSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContentSeeder.class);

    private static final String STATE_KEY = "content";

    private final ContentLoader contentLoader;
    private final CatalogStore catalogStore;
    private final TopicRepository topicRepository;
    private final TrackRepository trackRepository;
    private final QuizService quizService;
    private final InterviewService interviewService;
    private final JdbcTemplate jdbcTemplate;

    public ContentSeeder(
            ContentLoader contentLoader,
            CatalogStore catalogStore,
            TopicRepository topicRepository,
            TrackRepository trackRepository,
            QuizService quizService,
            InterviewService interviewService,
            JdbcTemplate jdbcTemplate
    ) {
        this.contentLoader = contentLoader;
        this.catalogStore = catalogStore;
        this.topicRepository = topicRepository;
        this.trackRepository = trackRepository;
        this.quizService = quizService;
        this.interviewService = interviewService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 启动后执行：内容版本变化时同步种子，否则保持数据库现状。
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        String version = contentLoader.readSeedVersion();
        String applied = appliedVersion();
        if (version.equals(applied)) {
            log.info("内容种子版本 {} 已是最新，跳过同步", version);
            return;
        }

        log.info("内容种子版本变化 {} -> {}，开始同步", applied == null ? "无" : applied, version);
        LocalDateTime now = LocalDateTime.now();
        int topics = syncTopics(now);
        int tracks = syncTracks(now);
        catalogStore.refresh();
        int quizzes = syncQuizzes();
        int interviews = syncInterviewQuestions();
        saveVersion(version, now);
        catalogStore.refresh();
        log.info("内容种子同步完成：学习项 {}，路线 {}，随堂题 {}，面试题 {}", topics, tracks, quizzes, interviews);
    }

    private int syncTopics(LocalDateTime now) throws IOException {
        List<ContentModels.TopicDocument> topics = contentLoader.readSeedTopics();
        for (ContentModels.TopicDocument doc : topics) {
            String payload = contentLoader.writeJson(doc);
            topicRepository.findBySlug(doc.slug()).ifPresentOrElse(entity -> {
                entity.apply(
                        doc.title(),
                        orDefault(doc.category(), "uncategorized"),
                        orDefault(doc.difficulty(), "beginner"),
                        orDefault(doc.status(), "draft"),
                        orDefault(doc.integrationStatus(), "docs_only"),
                        doc.estimatedHours(),
                        doc.summary(),
                        payload,
                        now
                );
                topicRepository.save(entity);
            }, () -> topicRepository.save(new TopicEntity(
                    doc.slug(),
                    doc.title(),
                    orDefault(doc.category(), "uncategorized"),
                    orDefault(doc.difficulty(), "beginner"),
                    orDefault(doc.status(), "draft"),
                    orDefault(doc.integrationStatus(), "docs_only"),
                    doc.estimatedHours(),
                    doc.summary(),
                    payload,
                    now
            )));
        }
        return topics.size();
    }

    private int syncTracks(LocalDateTime now) throws IOException {
        List<ContentModels.TrackDocument> tracks = contentLoader.readSeedTracks();
        for (ContentModels.TrackDocument doc : tracks) {
            String payload = contentLoader.writeJson(doc);
            trackRepository.findBySlug(doc.slug()).ifPresentOrElse(
                    entity -> {
                        entity.apply(doc.title(), doc.audience(), payload, now);
                        trackRepository.save(entity);
                    },
                    () -> trackRepository.save(new TrackEntity(
                            doc.slug(), doc.title(), doc.audience(), payload, now
                    ))
            );
        }
        return tracks.size();
    }

    private int syncQuizzes() throws IOException {
        int imported = 0;
        for (ContentLoader.SeedQuiz seed : contentLoader.readSeedQuizzes()) {
            if (!topicRepository.existsBySlug(seed.topicSlug())) {
                log.warn("跳过随堂题种子：学习项不存在 {}", seed.topicSlug());
                continue;
            }
            quizService.replace(seed.topicSlug(), seed.questions());
            imported += seed.questions().size();
        }
        return imported;
    }

    private int syncInterviewQuestions() throws IOException {
        List<ContentModels.InterviewSeed> seeds = contentLoader.readSeedInterviewQuestions();
        interviewService.syncSeeds(seeds.stream()
                .map(seed -> new InterviewDtos.QuestionRequest(
                        seed.category(),
                        seed.level(),
                        seed.type(),
                        seed.stem(),
                        seed.options(),
                        seed.correctIndex(),
                        seed.referenceAnswer(),
                        seed.explanation()
                ))
                .toList());
        return seeds.size();
    }

    private String appliedVersion() {
        List<String> values = jdbcTemplate.queryForList(
                "SELECT state_value FROM content_seed_state WHERE state_key = ?",
                String.class,
                STATE_KEY
        );
        return values.isEmpty() ? null : values.get(0);
    }

    private void saveVersion(String version, LocalDateTime now) {
        int updated = jdbcTemplate.update(
                "UPDATE content_seed_state SET state_value = ?, updated_at = ? WHERE state_key = ?",
                version, now, STATE_KEY
        );
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO content_seed_state (state_key, state_value, updated_at) VALUES (?, ?, ?)",
                    STATE_KEY, version, now
            );
        }
    }

    private String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
