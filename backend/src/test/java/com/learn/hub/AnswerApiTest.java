package com.learn.hub;

import com.fasterxml.jackson.databind.JsonNode;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 答题记录：判分、错题本、统计、删除与鉴权。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AnswerApiTest {

    private static final String AUTH = "Authorization";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String register(String username) throws Exception {
        String body = """
                {"username":"%s","password":"secret123","displayName":"答题测试"}
                """.formatted(username);
        String response = mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(response).path("token").asText();
    }

    private JsonNode questions() throws Exception {
        String response = mvc.perform(get("/api/v1/interview/questions").param("size", "100"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("items");
    }

    @Test
    void submitChoiceAndWrongBookFlow() throws Exception {
        String bearer = register("answer_choice");
        JsonNode choice = null;
        for (JsonNode item : questions()) {
            if ("choice".equals(item.path("type").asText())) {
                choice = item;
                break;
            }
        }
        if (choice == null) {
            throw new IllegalStateException("题库中没有选择题");
        }
        long id = choice.path("id").asLong();
        int correct = choice.path("correctIndex").asInt();
        int wrong = (correct + 1) % choice.path("options").size();

        mvc.perform(post("/api/v1/me/answers")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"interview\",\"questionId\":" + id
                                + ",\"choiceIndex\":" + correct + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));

        mvc.perform(post("/api/v1/me/answers")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"interview\",\"questionId\":" + id
                                + ",\"choiceIndex\":" + wrong + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.attempts").value(2));

        mvc.perform(get("/api/v1/me/answers").param("wrong", "true").header(AUTH, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].questionId").value(id));

        mvc.perform(get("/api/v1/me/answers/stats").header(AUTH, bearer))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.correct").value(0))
                .andExpect(jsonPath("$.wrong").value(1));

        mvc.perform(delete("/api/v1/me/answers/interview/" + id).header(AUTH, bearer))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/me/answers/stats").header(AUTH, bearer))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void textSelfAssessmentEntersWrongBook() throws Exception {
        String bearer = register("answer_text");
        JsonNode text = null;
        for (JsonNode item : questions()) {
            if ("text".equals(item.path("type").asText())) {
                text = item;
                break;
            }
        }
        if (text == null) {
            throw new IllegalStateException("题库中没有自答题");
        }
        long id = text.path("id").asLong();

        mvc.perform(post("/api/v1/me/answers")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"interview\",\"questionId\":" + id
                                + ",\"textAnswer\":\"我的回答\",\"selfCorrect\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false));

        mvc.perform(get("/api/v1/me/answers").param("wrong", "true").header(AUTH, bearer))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void resultFilterSplitsCorrectAndWrong() throws Exception {
        String bearer = register("answer_result");
        JsonNode choice = null;
        for (JsonNode item : questions()) {
            if ("choice".equals(item.path("type").asText())) {
                choice = item;
                break;
            }
        }
        if (choice == null) {
            throw new IllegalStateException("题库中没有选择题");
        }
        long id = choice.path("id").asLong();
        int correct = choice.path("correctIndex").asInt();
        int wrong = (correct + 1) % choice.path("options").size();

        mvc.perform(post("/api/v1/me/answers")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"interview\",\"questionId\":" + id
                                + ",\"choiceIndex\":" + correct + "}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/me/answers").param("result", "correct").header(AUTH, bearer))
                .andExpect(jsonPath("$.total").value(1));
        mvc.perform(get("/api/v1/me/answers").param("result", "wrong").header(AUTH, bearer))
                .andExpect(jsonPath("$.total").value(0));

        mvc.perform(post("/api/v1/me/answers")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"interview\",\"questionId\":" + id
                                + ",\"choiceIndex\":" + wrong + "}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/me/answers").param("result", "correct").header(AUTH, bearer))
                .andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/v1/me/answers").param("result", "wrong").header(AUTH, bearer))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void answerRequiresLogin() throws Exception {
        mvc.perform(get("/api/v1/me/answers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidSourceIsBadRequest() throws Exception {
        String bearer = register("answer_bad");
        mvc.perform(post("/api/v1/me/answers")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"nope\",\"questionId\":1}"))
                .andExpect(status().isBadRequest());
    }
}
