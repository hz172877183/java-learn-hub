package com.learn.hub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 管理端 CRUD、发布校验、题目替换与错误语义的集成测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminAndQuizTest {

    private static final String ADMIN = "Bearer test-admin-token";
    private static final String AUTH = "Authorization";

    @Autowired
    private MockMvc mvc;

    private static String uniqueSlug(String prefix) {
        return prefix + (System.nanoTime() % 1_000_000_000L);
    }

    private static String draftBody(String slug) {
        return """
                {
                  "slug": "%s",
                  "title": "集成测试学习项",
                  "category": "engineering",
                  "summary": "测试用",
                  "difficulty": "beginner",
                  "status": "draft",
                  "integrationStatus": "docs_only",
                  "overviewMd": "",
                  "tags": ["test"],
                  "concepts": [],
                  "pathSteps": [],
                  "samples": [],
                  "pitfalls": [],
                  "repoPointers": [],
                  "scenarios": []
                }
                """.formatted(slug);
    }

    private static String publishedBody(String slug) {
        return """
                {
                  "slug": "%s",
                  "title": "集成测试学习项（已发布）",
                  "category": "engineering",
                  "summary": "测试用",
                  "difficulty": "beginner",
                  "status": "published",
                  "integrationStatus": "runtime",
                  "overviewMd": "## 概述",
                  "tags": ["test"],
                  "concepts": [{"term": "A", "explanation": "B"}],
                  "pathSteps": [{"title": "第一步", "goal": "跑通", "hours": 1, "resources": []}],
                  "samples": [{"title": "示例", "language": "java", "description": "d", "code": "1;"}],
                  "pitfalls": [
                    {"title": "坑1", "detail": "d1"},
                    {"title": "坑2", "detail": "d2"},
                    {"title": "坑3", "detail": "d3"}
                  ],
                  "repoPointers": [],
                  "scenarios": [{"title": "场景", "signal": "信号", "approach": "做法"}]
                }
                """.formatted(slug);
    }

    @Test
    void adminListRequiresToken() throws Exception {
        mvc.perform(get("/api/v1/admin/topics"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void createPublishDeleteFlow() throws Exception {
        String slug = uniqueSlug("it-topic-");

        mvc.perform(post("/api/v1/admin/topics")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody(slug)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value(slug));

        mvc.perform(get("/api/v1/topics/" + slug))
                .andExpect(status().isNotFound());

        mvc.perform(put("/api/v1/admin/topics/" + slug)
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishedBody(slug)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("published"));

        mvc.perform(get("/api/v1/topics/" + slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenarios.length()").value(1));

        mvc.perform(delete("/api/v1/admin/topics/" + slug).header(AUTH, ADMIN))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/topics/" + slug))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateSlugIsConflict() throws Exception {
        String slug = uniqueSlug("it-dup-");
        mvc.perform(post("/api/v1/admin/topics")
                .header(AUTH, ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(draftBody(slug))).andExpect(status().isCreated());

        mvc.perform(post("/api/v1/admin/topics")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody(slug)))
                .andExpect(status().isConflict());
    }

    @Test
    void incompletePublishIsBadRequest() throws Exception {
        String slug = uniqueSlug("it-bad-");

        mvc.perform(post("/api/v1/admin/topics")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftBody(slug).replace("\"draft\"", "\"published\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void quizPublicListContainsAnswerAndExplanation() throws Exception {
        mvc.perform(get("/api/v1/topics/actuator/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("choice"))
                .andExpect(jsonPath("$[0].correctIndex").isNumber())
                .andExpect(jsonPath("$[0].explanation").isNotEmpty());
    }

    @Test
    void quizReplaceKeepsOrderAndContent() throws Exception {
        String body = """
                [
                  {"type":"choice","stem":"新题 1","options":["A","B","C","D"],"correctIndex":2,"explanation":"解析 1"},
                  {"type":"text","stem":"新题 2","options":[],"referenceAnswer":"参考答案","explanation":"解析 2"}
                ]
                """;

        mvc.perform(put("/api/v1/admin/topics/actuator/quizzes")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(get("/api/v1/topics/actuator/quizzes"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].stem").value("新题 1"))
                .andExpect(jsonPath("$[0].correctIndex").value(2))
                .andExpect(jsonPath("$[1].stem").value("新题 2"))
                .andExpect(jsonPath("$[1].referenceAnswer").value("参考答案"));
    }

    @Test
    void quizWithoutExplanationIsBadRequest() throws Exception {
        String body = """
                [{"type":"choice","stem":"缺解析","options":["A","B"],"correctIndex":0}]
                """;

        mvc.perform(put("/api/v1/admin/topics/actuator/quizzes")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void quizOfUnknownTopicIsNotFound() throws Exception {
        mvc.perform(get("/api/v1/topics/nope/quizzes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void wrongMethodIs405() throws Exception {
        mvc.perform(post("/api/v1/admin/topics/actuator").header(AUTH, ADMIN))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void wrongContentTypeIs415() throws Exception {
        mvc.perform(post("/api/v1/admin/topics")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mvc.perform(post("/api/v1/admin/topics")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad"))
                .andExpect(status().isBadRequest());
    }
}
