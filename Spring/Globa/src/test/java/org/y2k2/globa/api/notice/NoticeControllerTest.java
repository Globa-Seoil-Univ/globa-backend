package org.y2k2.globa.api.notice;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.api.NoticeController;
import org.y2k2.globa.application.notice.dto.request.RequestNoticeAddDto;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeDetailDto;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeIntroDto;
import org.y2k2.globa.application.notice.service.CreateNoticeService;
import org.y2k2.globa.application.notice.service.GetIntroNoticesService;
import org.y2k2.globa.application.notice.service.GetNoticeDetailService;
import org.y2k2.globa.common.util.jwt.JWT;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = NoticeController.class)
public class NoticeControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetIntroNoticesService getIntroNoticesService;
    @MockBean
    private GetNoticeDetailService getNoticeDetailService;
    @MockBean
    private CreateNoticeService createNoticeService;

    @Test
    @DisplayName("간단 공지사항 조회 - 성공")
    @WithAccount
    void getIntroNotices_Success() throws Exception {
        ResponseNoticeIntroDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseNoticeIntroDto.class);

        Mockito
                .when(getIntroNoticesService.get())
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/notice/intro")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("공지사항 상세 조회 - 성공")
    @WithAccount
    void getNoticeDetail_Success() throws Exception {
        ResponseNoticeDetailDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseNoticeDetailDto.class);

        Mockito
                .when(getNoticeDetailService.get(Mockito.anyLong()))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/notice/1")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("공지사항 등록 - 성공")
    @WithAccount
    void createNotice_Success() throws Exception {
        MockMultipartFile mockThumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        Mockito
                .when(createNoticeService.create(Mockito.any(RequestNoticeAddDto.class), Mockito.anyLong()))
                .thenReturn(1L);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/notice")
                                .file(mockThumbnail)
                                .param("title", "Test Notice Title")
                                .param("content", "This is a test notice content.")
                                .param("bgColor", "#FFFFFF")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @DisplayName("공지사항 등록 - 실패 (잘못된 요청)")
    @WithAccount
    void createNotice_Failure_InvalidRequest() throws Exception {
        MockMultipartFile mockThumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/notice")
                                .file(mockThumbnail)
                                .param("title", "") // 빈 제목
                                .param("content", "This is a test notice content.")
                                .param("bgColor", "#FFFFFF")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
