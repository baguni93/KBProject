package org.scoula.event.controller;


import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.config.RootConfig;
import org.scoula.config.ServletConfig;
import org.scoula.event.service.EventService;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        RootConfig.class,
        ServletConfig.class,
        SecurityConfig.class
})
@Log4j2
public class EventControllerTest {
    @Autowired
    EventService service;

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mockMvc;


    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Test
    @DisplayName("1. 이벤트 메인화면 조회 테스트")
    public void getEventMainPage() throws Exception {
        Integer userId = 1;

        String jsonResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/event/main")
                                .param("userId", String.valueOf(userId))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        log.info("테스트 결과 ====================");
        log.info(jsonResponse);
    }


    @Test
    @DisplayName("2. 진행 중인 이벤트 전체 리스트 조회 테스트")
    public void getActiveEventsProgress() throws Exception {
        Integer userId = 1;

        String jsonResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/event/list")
                                .param("userId", String.valueOf(userId))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        log.info("2. 진행중 이벤트 리스트 결과 ====================");
        log.info(jsonResponse);
    }

    @Test
    @DisplayName("3. 참여완료 이벤트 리스트 조회 테스트")
    public void getJoinedEventsProgress() throws Exception {
        Integer userId = 1;

        String jsonResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/event/joined")
                                .param("userId", String.valueOf(userId))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        log.info("3. 참여 완료 이벤트 리스트 결과 ====================");
        log.info(jsonResponse);
    }
}
