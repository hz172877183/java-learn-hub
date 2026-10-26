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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 登录用户的面试题：默认排除已答、内联 myAnswer、只看错题与按范围重置。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MeInterviewApiTest {

    private static final String AUTH = "Authorization";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String register(String username) throws Exception {
        String body = """
                {"username":"%s","password":"secret123","displayName":"我的面试题"}
                """.formatted(username);
        String response = mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(response).path("token").asText();
    }

    private JsonNode questions(String bearer, String query) throws Exception {
        String response = mvc.perform(get("/api/v1/me/interview/questions" + query).header(AUTH, bearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode find(JsonNode list, long id) {
        for (JsonNode item : list.path("items")) {
            if (item.path("id").asLong() == id) {
                return item;
            }
        }
        return null;
    }

    private JsonNode firstChoice(JsonNode list) {
        for (JsonNode item : list.path("items")) {
            if ("choice".equals(item.path("type").asText())) {
                return item;
            }
        }
        return null;
    }

    private void answer(String bearer, long id, int choiceIndex) throws Exception {
        mvc.perform(post("/api/v1/me/answers")
                        .header(AUTH, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\":\"interview\",\"questionId\":" + id
                                + ",\"choiceIndex\":" + choiceIndex + "}"))
                .andExpect(status().isOk());
    }

    private int reset(String bearer, String query) throws Exception {
        String response = mvc.perform(delete("/api/v1/me/interview/answers" + query).header(AUTH, bearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("deleted").asInt();
    }

    @Test
    void requiresLogin() throws Exception {
        mvc.perform(get("/api/v1/me/interview/questions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void answeredQuestionIsHiddenThenEchoedWhenIncluded() throws Exception {
        String bearer = register("me_iv_echo");
        JsonNode initial = questions(bearer, "?size=100");
        JsonNode choice = firstChoice(initial);
        assertNotNull(choice);
        long id = choice.path("id").asLong();
        int correct = choice.path("correctIndex").asInt();
        int wrong = (correct + 1) % choice.path("options").size();

        answer(bearer, id, correct);

        JsonNode hidden = questions(bearer, "?size=100");
        assertFalse(find(hidden, id) != null);

        JsonNode included = questions(bearer, "?size=100&includeAnswered=true");
        JsonNode echoed = find(included, id);
        assertNotNull(echoed);
        assertEquals(correct, echoed.path("myAnswer").path("choiceIndex").asInt());
        assertTrue(echoed.path("myAnswer").path("correct").asBoolean());

        JsonNode wrongOnlyAfterCorrect = questions(bearer, "?size=100&wrongOnly=true");
        assertFalse(find(wrongOnlyAfterCorrect, id) != null);

        answer(bearer, id, wrong);
        JsonNode wrongOnlyAfterWrong = questions(bearer, "?size=100&wrongOnly=true");
        assertNotNull(find(wrongOnlyAfterWrong, id));
    }

    @Test
    void resetByLevelDeletesOnlyMatchingAnswers() throws Exception {
        String bearer = register("me_iv_reset");
        JsonNode junior = firstChoice(questions(bearer, "?level=junior&size=100"));
        JsonNode senior = firstChoice(questions(bearer, "?level=senior&size=100"));
        assertNotNull(junior);
        assertNotNull(senior);
        long juniorId = junior.path("id").asLong();
        long seniorId = senior.path("id").asLong();

        answer(bearer, juniorId, junior.path("correctIndex").asInt());
        answer(bearer, seniorId, senior.path("correctIndex").asInt());

        assertEquals(1, reset(bearer, "?level=junior"));

        assertNotNull(find(questions(bearer, "?level=junior&size=100"), juniorId));
        assertFalse(find(questions(bearer, "?level=senior&size=100"), seniorId) != null);

        assertEquals(1, reset(bearer, ""));
        assertNotNull(find(questions(bearer, "?level=senior&size=100"), seniorId));
    }
}
