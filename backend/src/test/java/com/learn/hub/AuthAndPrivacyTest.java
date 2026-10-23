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
 * 认证、进度、笔记与用户隔离的集成测试。
 * 依赖本机 MySQL（learnhub_test 库），Flyway 自动建表，每个用例事务回滚。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthAndPrivacyTest {

    private static final String AUTH = "Authorization";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String unique(String prefix) {
        return prefix + (System.nanoTime() % 1_000_000_000L);
    }

    private String register(String username) throws Exception {
        String body = """
                {"username":"%s","password":"secret123","displayName":"测试用户"}
                """.formatted(username);
        String response = mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("token").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void registerAndLogin() throws Exception {
        String username = unique("user");
        register(username);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value(username));
    }

    @Test
    void duplicateUsernameIsConflict() throws Exception {
        String username = unique("dup");
        register(username);

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USERNAME_TAKEN"));
    }

    @Test
    void invalidUsernameIsBadRequest() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ab\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        String username = unique("pwd");
        register(username);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meRequiresToken() throws Exception {
        mvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        String username = unique("me");
        String token = register(username);

        mvc.perform(get("/api/v1/me").header(AUTH, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void progressLifecycle() throws Exception {
        String token = register(unique("prog"));

        mvc.perform(put("/api/v1/me/progress/redis")
                        .header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"state\":\"learning\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("learning"))
                .andExpect(jsonPath("$.startedAt").isNotEmpty())
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        mvc.perform(put("/api/v1/me/progress/redis")
                        .header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"state\":\"completed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("completed"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        mvc.perform(get("/api/v1/me/progress").header(AUTH, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].topicSlug").value("redis"));

        mvc.perform(delete("/api/v1/me/progress/redis").header(AUTH, bearer(token)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/me/progress").header(AUTH, bearer(token)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void invalidProgressStateIsBadRequest() throws Exception {
        String token = register(unique("badstate"));

        mvc.perform(put("/api/v1/me/progress/redis")
                        .header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"state\":\"nope\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownTopicIsNotFound() throws Exception {
        String token = register(unique("unknown"));

        mvc.perform(put("/api/v1/me/progress/not-a-topic")
                        .header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"state\":\"learning\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void noteUpsertAndClear() throws Exception {
        String token = register(unique("note"));

        mvc.perform(put("/api/v1/me/notes/redis")
                        .header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"# 标题\\n内容\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("# 标题\n内容"));

        mvc.perform(get("/api/v1/me/notes/redis").header(AUTH, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("# 标题\n内容"));

        mvc.perform(get("/api/v1/me/notes").header(AUTH, bearer(token)))
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(put("/api/v1/me/notes/redis")
                        .header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(""));

        mvc.perform(get("/api/v1/me/notes").header(AUTH, bearer(token)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void tooLongNoteIsBadRequest() throws Exception {
        String token = register(unique("long"));
        String body = "{\"content\":\"" + "x".repeat(100_001) + "\"}";

        mvc.perform(put("/api/v1/me/notes/redis")
                        .header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usersCannotSeeEachOtherProgressOrNotes() throws Exception {
        String alice = register(unique("alice"));
        String bob = register(unique("bob"));

        mvc.perform(put("/api/v1/me/progress/redis")
                .header(AUTH, bearer(alice))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"state\":\"completed\"}")).andExpect(status().isOk());
        mvc.perform(put("/api/v1/me/notes/redis")
                .header(AUTH, bearer(alice))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"alice private\"}")).andExpect(status().isOk());

        mvc.perform(get("/api/v1/me/progress").header(AUTH, bearer(bob)))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/me/notes").header(AUTH, bearer(bob)))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/me/notes/redis").header(AUTH, bearer(bob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(""));
    }
}
