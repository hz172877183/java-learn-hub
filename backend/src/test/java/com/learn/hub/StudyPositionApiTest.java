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
 * 学习位置：保存、读取、覆盖、清除、越权隔离与 scope 校验。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudyPositionApiTest {

    private static final String AUTH = "Authorization";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String register(String username) throws Exception {
        String body = """
                {"username":"%s","password":"secret123","displayName":"位置测试"}
                """.formatted(username);
        String response = mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(response).path("token").asText();
    }

    private static String positionBody(int page, Long questionId) {
        return """
                {
                  "level": "senior",
                  "category": "mysql",
                  "q": "索引",
                  "page": %d,
                  "size": 20,
                  "questionId": %s,
                  "scrollTop": 860
                }
                """.formatted(page, questionId == null ? "null" : questionId);
    }

    @Test
    void positionRequiresLogin() throws Exception {
        mvc.perform(get("/api/v1/me/positions/interview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void saveReadOverwriteAndDelete() throws Exception {
        String bearer = register("position_main");

        mvc.perform(get("/api/v1/me/positions/interview").header(AUTH, bearer))
                .andExpect(status().isNoContent());

        mvc.perform(put("/api/v1/me/positions/interview")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(positionBody(2, 1234L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.questionId").value(1234));

        mvc.perform(get("/api/v1/me/positions/interview").header(AUTH, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("senior"))
                .andExpect(jsonPath("$.category").value("mysql"))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.scrollTop").value(860));

        mvc.perform(put("/api/v1/me/positions/interview")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(positionBody(3, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(3));

        mvc.perform(get("/api/v1/me/positions/interview").header(AUTH, bearer))
                .andExpect(jsonPath("$.page").value(3))
                .andExpect(jsonPath("$.questionId").doesNotExist());

        mvc.perform(delete("/api/v1/me/positions/interview").header(AUTH, bearer))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/me/positions/interview").header(AUTH, bearer))
                .andExpect(status().isNoContent());
    }

    @Test
    void usersDoNotSharePosition() throws Exception {
        String alice = register("position_alice");
        String bob = register("position_bob");

        mvc.perform(put("/api/v1/me/positions/interview")
                .header(AUTH, alice)
                .contentType(MediaType.APPLICATION_JSON)
                .content(positionBody(2, 99L))).andExpect(status().isOk());

        mvc.perform(get("/api/v1/me/positions/interview").header(AUTH, bob))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidScopeIsBadRequest() throws Exception {
        String bearer = register("position_bad");
        mvc.perform(put("/api/v1/me/positions/all")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(positionBody(1, null)))
                .andExpect(status().isBadRequest());
    }
}
