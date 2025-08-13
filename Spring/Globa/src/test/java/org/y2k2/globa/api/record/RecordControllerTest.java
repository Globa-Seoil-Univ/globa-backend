package org.y2k2.globa.api.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.api.RecordController;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.record.dto.request.RequestPostRecordDto;
import org.y2k2.globa.application.record.dto.request.RequestRecordMoveDto;
import org.y2k2.globa.application.record.dto.request.RequestRecordNameDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordDetailDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordSearchDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsByFolderDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
import org.y2k2.globa.application.record.service.*;
import org.y2k2.globa.application.study.dto.request.RequestStudyDto;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.infrastructure.persistence.record.type.Language;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@WebMvcTest(controllers = RecordController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RecordControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetRecordsService getRecordsService;
    @MockBean
    private GetRecentRecordsService getRecentRecordsService;
    @MockBean
    private GetRecordService getRecordService;
    @MockBean
    private GetAnalysisService getAnalysisService;
    @MockBean
    private SearchRecordService searchRecordService;
    @MockBean
    private GetReceivingRecordsService getReceivingRecordsService;
    @MockBean
    private GetSharingRecordsService getSharingRecordsService;
    @MockBean
    private CreateRecordService createRecordService;
    @MockBean
    private UpdateShareLinkStatusService updateShareLinkStatusService;
    @MockBean
    private UpdateRecordNameService updateRecordNameService;
    @MockBean
    private MoveRecordService moveRecordService;
    @MockBean
    private UpsertStudyService upsertStudyService;
    @MockBean
    private DeleteRecordService deleteRecordService;

    @Test
    @DisplayName("폴더 내 문서 목록 조회 - 성공")
    @WithAccount
    void getRecordByFolderId() throws Exception {
        Long folderId = 1L;
        Long userId = 1L;
        int page = 1;
        int count = 10;
        ResponseRecordsByFolderDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseRecordsByFolderDto.class);

        Mockito.when(getRecordsService.get(folderId, page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get(Constant.RECORD_PREFIX.getValue(), folderId)
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .param("page", String.valueOf(page))
                        .param("count", String.valueOf(count))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getRecordsService, Mockito.times(1))
                .get(folderId, page, count, userId);
    }

    @Test
    @DisplayName("폴더 내 문서 목록 조회 - 성공 (문서 없음)")
    @WithAccount
    void getRecordByFolderIdEmpty() throws Exception {
        Long folderId = 1L;
        Long userId = 1L;
        int page = 1;
        int count = 10;
        ResponseRecordsByFolderDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(ResponseRecordsByFolderDto.class)
                .set("records", new ArrayList<>())
                .set("isOwner", true)
                .set("total", 0L)
                .sample();

        Mockito.when(getRecordsService.get(folderId, page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(Constant.RECORD_PREFIX.getValue(), folderId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());


        log.info("response = {}", response);

        Mockito.verify(getRecordsService, Mockito.times(1))
                .get(folderId, page, count, userId);
    }

    @Test
    @DisplayName("최근 문서 목록 조회 - 성공")
    @WithAccount
    void getRecentRecord() throws Exception {
        Long userId = 1L;
        int page = 1;
        int count = 10;
        ResponseRecordsDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseRecordsDto.class);

        Mockito.when(getRecentRecordsService.get(page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/record/recent")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getRecentRecordsService, Mockito.times(1))
                .get(page, count, userId);
    }

    @Test
    @DisplayName("최근 문서 목록 조회 - 성공 (문서 없음)")
    @WithAccount
    void getRecentRecordEmpty() throws Exception {
        Long userId = 1L;
        int page = 1;
        int count = 10;
        ResponseRecordsDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(ResponseRecordsDto.class)
                .set("records", new ArrayList<>())
                .set("total", 0L)
                .sample();

        Mockito.when(getRecentRecordsService.get(page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/record/recent")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getRecentRecordsService, Mockito.times(1))
                .get(page, count, userId);
    }

    @Test
    @DisplayName("문서 상세 조회 - 성공")
    @WithAccount
    void getRecord() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        ResponseRecordDetailDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseRecordDetailDto.class);

        Mockito.when(getRecordService.get(folderId, recordId, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(Constant.RECORD_PREFIX.getValue() + "/{recordId}", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getRecordService, Mockito.times(1))
                .get(folderId, recordId, userId);
    }

    @Test
    @DisplayName("문서 내 시각화 조회 - 성공")
    @WithAccount
    void getAnalysis() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        ResponseAnalysisDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseAnalysisDto.class);

        Mockito.when(getAnalysisService.get(folderId, recordId, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(Constant.RECORD_PREFIX.getValue() + "/{recordId}/analysis", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getAnalysisService, Mockito.times(1))
                .get(folderId, recordId, userId);
    }

    @Test
    @DisplayName("문서 내 시각화 조회 - 성공 (기록 없음)")
    @WithAccount
    void getAnalysisEmpty() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        ResponseAnalysisDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(ResponseAnalysisDto.class)
                .set("keywords", new ArrayList<>())
                .set("studyTimes", new ArrayList<>())
                .set("quizGrades", new ArrayList<>())
                .sample();

        Mockito.when(getAnalysisService.get(folderId, recordId, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(Constant.RECORD_PREFIX.getValue() + "/{recordId}/analysis", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getAnalysisService, Mockito.times(1))
                .get(folderId, recordId, userId);
    }

    @Test
    @DisplayName("문서 검색 - 성공")
    @WithAccount
    void searchRecord() throws Exception {
        Long userId = 1L;
        int page = 1;
        int count = 10;
        String keyword = "test";
        ResponseRecordSearchDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseRecordSearchDto.class);

        Mockito.when(searchRecordService.search(keyword, page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/record/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("keyword", keyword)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(searchRecordService, Mockito.times(1))
                .search(keyword, page, count, userId);
    }

    @Test
    @DisplayName("문서 검색 - 성공 (문서 없음)")
    @WithAccount
    void searchRecordEmpty() throws Exception {
        Long userId = 1L;
        int page = 1;
        int count = 10;
        String keyword = "test";
        ResponseRecordSearchDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(ResponseRecordSearchDto.class)
                .set("records", new ArrayList<>())
                .set("total", 0L)
                .sample();

        Mockito.when(searchRecordService.search(keyword, page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/record/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("keyword", keyword)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(searchRecordService, Mockito.times(1))
                .search(keyword, page, count, userId);
    }

    @Test
    @DisplayName("문서 검색 - 성공 (키워드 없음)")
    @WithAccount
    void searchRecordNoKeyword() throws Exception {
        Long userId = 1L;
        int page = 1;
        int count = 10;
        String keyword = "";
        ResponseRecordSearchDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(ResponseRecordSearchDto.class)
                .set("records", new ArrayList<>())
                .set("total", 0L)
                .sample();

        Mockito.when(searchRecordService.search(keyword, page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/record/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(searchRecordService, Mockito.times(1))
                .search(keyword, page, count, userId);
    }

    @Test
    @DisplayName("공유 받는 문서 목록 조회 - 성공")
    @WithAccount
    void getReceivingRecord() throws Exception {
        Long userId = 1L;
        int page = 1;
        int count = 10;
        ResponseRecordsDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseRecordsDto.class);

        Mockito.when(getReceivingRecordsService.get(page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/record/receiving")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getReceivingRecordsService, Mockito.times(1))
                .get(page, count, userId);
    }

    @Test
    @DisplayName("공유 받는 문서 목록 조회 - 성공 (문서 없음)")
    @WithAccount
    void getReceivingRecordEmpty() throws Exception {
        Long userId = 1L;
        int page = 1;
        int count = 10;
        ResponseRecordsDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(ResponseRecordsDto.class)
                .set("records", new ArrayList<>())
                .set("total", 0L)
                .sample();

        Mockito.when(getReceivingRecordsService.get(page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/record/receiving")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getReceivingRecordsService, Mockito.times(1))
                .get(page, count, userId);
    }

    @Test
    @DisplayName("공유 하는 문서 목록 조회 - 성공")
    @WithAccount
    void getSharingRecord() throws Exception {
        Long userId = 1L;
        int page = 1;
        int count = 10;
        ResponseRecordsDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseRecordsDto.class);

        Mockito.when(getSharingRecordsService.get(page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/record/sharing")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getSharingRecordsService, Mockito.times(1))
                .get(page, count, userId);
    }

    @Test
    @DisplayName("공유 하는 문서 목록 조회 - 성공 (문서 없음)")
    @WithAccount
    void getSharingRecordEmpty() throws Exception {
        Long userId = 1L;
        int page = 1;
        int count = 10;
        ResponseRecordsDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(ResponseRecordsDto.class)
                .set("records", new ArrayList<>())
                .set("total", 0L)
                .sample();

        Mockito.when(getSharingRecordsService.get(page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/record/sharing")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getSharingRecordsService, Mockito.times(1))
                .get(page, count, userId);
    }

    @Test
    @DisplayName("문서 생성 - 성공")
    @WithAccount
    void createRecord() throws Exception {
        Long folderId = 1L;
        Long userId = 1L;
        RequestPostRecordDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestPostRecordDto.class)
                .set("lang", Language.KO.name())
                .sample();

        log.info("request = {}", request);

        Mockito.doNothing()
                .when(createRecordService)
                .create(folderId, request, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(Constant.RECORD_PREFIX.getValue(), folderId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(createRecordService, Mockito.times(1))
                .create(folderId, request, userId);
    }

    @Test
    @DisplayName("문서 생성 - 실패 (잘못된 요청)")
    @WithAccount
    void createRecordFail() throws Exception {
        Long folderId = 1L;
        Long userId = 1L;

        // title, path 중 하나가 null인 경우
        RequestPostRecordDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestPostRecordDto.class)
                .set("title", null)
                .sample();

        log.info("request = {}", request);

        Mockito.doNothing()
                .when(createRecordService)
                .create(folderId, request, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(Constant.RECORD_PREFIX.getValue(), folderId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });

        Mockito.verify(createRecordService, Mockito.times(0))
                .create(folderId, request, userId);
    }

    @Test
    @DisplayName("문서 공유 링크 생성 - 성공")
    @WithAccount
    void createShareLink() throws Exception {
        boolean isShare = true;
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;

        Mockito.doNothing()
                .when(updateShareLinkStatusService)
                .update(folderId, recordId, isShare, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(Constant.RECORD_PREFIX.getValue() + "/{recordId}/link", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(updateShareLinkStatusService, Mockito.times(1))
                .update(folderId, recordId, isShare, userId);
    }

    @Test
    @DisplayName("문서 이름 수정 - 성공")
    @WithAccount
    void updateRecordName() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        RequestRecordNameDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestRecordNameDto.class);

        Mockito.doNothing()
                .when(updateRecordNameService)
                .update(folderId, recordId, request.title(), userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .patch(Constant.RECORD_PREFIX.getValue() + "/{recordId}/name", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(updateRecordNameService, Mockito.times(1))
                .update(folderId, recordId, request.title(), userId);
    }

    @Test
    @DisplayName("문서 이름 수정 - 실패 (이름 없음)")
    @WithAccount
    void updateRecordNameFail() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;

        RequestRecordNameDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestRecordNameDto.class)
                .set("title", null)
                .sample();

        Mockito.doNothing()
                .when(updateRecordNameService)
                .update(folderId, recordId, request.title(), userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .patch(Constant.RECORD_PREFIX.getValue() + "/{recordId}/name", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });

        Mockito.verify(updateRecordNameService, Mockito.times(0))
                .update(folderId, recordId, request.title(), userId);
    }

    @Test
    @DisplayName("문서 이동 - 성공")
    @WithAccount
    void moveRecord() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        RequestRecordMoveDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestRecordMoveDto.class);

        Mockito.doNothing()
                .when(moveRecordService)
                .move(folderId, recordId, request, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .patch(Constant.RECORD_PREFIX.getValue() + "/{recordId}/move", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(moveRecordService, Mockito.times(1))
                .move(folderId, recordId, request, userId);
    }

    @Test
    @DisplayName("문서 이동 - 실패 (대상 폴더 값 없음)")
    @WithAccount
    void moveRecordFail() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;

        RequestRecordMoveDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestRecordMoveDto.class)
                .set("targetId", null)
                .sample();

        Mockito.doNothing()
                .when(moveRecordService)
                .move(folderId, recordId, request, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .patch(Constant.RECORD_PREFIX.getValue() + "/{recordId}/move", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });

        Mockito.verify(moveRecordService, Mockito.times(0))
                .move(folderId, recordId, request, userId);
    }

    @Test
    @DisplayName("공부 기록 생성 또는 수정 - 성공")
    @WithAccount
    void upsertStudyTime() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        RequestStudyDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestStudyDto.class);

        Mockito.doNothing()
                .when(upsertStudyService)
                .upsert(folderId, recordId, request, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .patch(Constant.RECORD_PREFIX.getValue() + "/{recordId}/study", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(upsertStudyService, Mockito.times(1))
                .upsert(folderId, recordId, request, userId);
    }

    @Test
    @DisplayName("공부 기록 저장 또는 수정 - 실패 (공부 시간 없음)")
    @WithAccount
    void upsertStudyTimeFail() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;

        RequestStudyDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestStudyDto.class)
                .set("studyTime", null)
                .sample();

        Mockito.doNothing()
                .when(upsertStudyService)
                .upsert(folderId, recordId, request, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .patch(Constant.RECORD_PREFIX.getValue() + "/{recordId}/study", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });;

        Mockito.verify(upsertStudyService, Mockito.times(0))
                .upsert(folderId, recordId, request, userId);
    }

    @Test
    @DisplayName("공부 시간 생성 또는 수정 - 실패 (공부 시간 0 미만)")
    @WithAccount
    void upsertStudyTimeFailNegative() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;

        RequestStudyDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestStudyDto.class)
                .set("studyTime", -1)
                .sample();

        Mockito.doNothing()
                .when(upsertStudyService)
                .upsert(folderId, recordId, request, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .patch(Constant.RECORD_PREFIX.getValue() + "/{recordId}/study", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });

        Mockito.verify(upsertStudyService, Mockito.times(0))
                .upsert(folderId, recordId, request, userId);
    }

    @Test
    @DisplayName("문서 삭제 - 성공")
    @WithAccount
    void deleteRecord() throws Exception {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;

        Mockito.doNothing()
                .when(deleteRecordService)
                .delete(folderId, recordId, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .delete(Constant.RECORD_PREFIX.getValue() + "/{recordId}", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(deleteRecordService, Mockito.times(1))
                .delete(folderId, recordId, userId);
    }

    @Test
    @DisplayName("문서 공유 링크 삭제 - 성공")
    @WithAccount
    void deleteShareLink() throws Exception {
        boolean isShare = false;
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;

        Mockito.doNothing()
                .when(updateShareLinkStatusService)
                .update(folderId, recordId, isShare, userId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .delete(Constant.RECORD_PREFIX.getValue() + "/{recordId}/link", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(updateShareLinkStatusService, Mockito.times(1))
                .update(folderId, recordId, isShare, userId);
    }
}
