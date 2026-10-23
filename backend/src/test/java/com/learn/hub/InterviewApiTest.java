package com.learn.hub;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 面试题专区：公开筛选、统计、管理端 CRUD 与校验。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InterviewApiTest {

    private static final String ADMIN = "Bearer test-admin-token";
    private static final String AUTH = "Authorization";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String choiceBody(String level) {
        return """
                {
                  "category": "mysql",
                  "level": "%s",
                  "type": "choice",
                  "stem": "集成测试：InnoDB 默认隔离级别是？",
                  "options": ["READ COMMITTED", "REPEATABLE READ", "SERIALIZABLE"],
                  "correctIndex": 1,
                  "explanation": "默认 REPEATABLE READ，用 MVCC 与间隙锁处理幻读。"
                }
                """.formatted(level);
    }

    @Test
    void publicListFiltersByLevel() throws Exception {
        mvc.perform(get("/api/v1/interview/questions").param("level", "senior").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(6))
                .andExpect(jsonPath("$.items[0].level").value("senior"));
    }

    @Test
    void metaContainsAllLevels() throws Exception {
        mvc.perform(get("/api/v1/interview/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.levels.length()").value(4))
                .andExpect(jsonPath("$.levels[0].id").value("junior"))
                .andExpect(jsonPath("$.total").value(24));
    }

    @Test
    void adminListRequiresToken() throws Exception {
        mvc.perform(get("/api/v1/admin/interview/questions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCrudFlow() throws Exception {
        String response = mvc.perform(post("/api/v1/admin/interview/questions")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(choiceBody("mid")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value("mid"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).path("id").asLong();

        mvc.perform(put("/api/v1/admin/interview/questions/" + id)
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(choiceBody("senior")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("senior"));

        mvc.perform(delete("/api/v1/admin/interview/questions/" + id).header(AUTH, ADMIN))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/admin/interview/questions/" + id).header(AUTH, ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidLevelIsBadRequest() throws Exception {
        mvc.perform(post("/api/v1/admin/interview/questions")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(choiceBody("expert")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void textWithoutReferenceIsBadRequest() throws Exception {
        String body = """
                {
                  "category": "spring",
                  "level": "junior",
                  "type": "text",
                  "stem": "什么是 IoC？",
                  "options": [],
                  "explanation": "缺少参考答案应被拒绝"
                }
                """;
        mvc.perform(post("/api/v1/admin/interview/questions")
                        .header(AUTH, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
