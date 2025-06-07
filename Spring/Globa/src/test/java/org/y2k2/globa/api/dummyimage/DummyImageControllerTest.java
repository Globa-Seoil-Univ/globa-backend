package org.y2k2.globa.api.dummyimage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
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
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.CommentController;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.api.DummyImageController;
import org.y2k2.globa.application.dummyimage.dto.request.RequestDummyImageDto;
import org.y2k2.globa.application.dummyimage.dto.response.ResponseDummyImageDto;
import org.y2k2.globa.application.dummyimage.service.CreateDummyImageService;
import org.y2k2.globa.common.util.jwt.JWT;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@WebMvcTest(controllers = DummyImageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DummyImageControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateDummyImageService createDummyImageService;

    @Test
    @DisplayName("더미 이미지 생성 - 성공")
    @WithAccount
    void createDummyImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                "test image content".getBytes()
        );

        ResponseDummyImageDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseDummyImageDto.class);

        Mockito
                .when(createDummyImageService.create(Mockito.any(RequestDummyImageDto.class), Mockito.anyLong()))
                .thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/dummy/image")
                        .file(file)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.MULTIPART_FORM_DATA)
        )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("더미 이미지 생성 - 실패 (파일 X)")
    void createDummyImage_Fail_NoFile() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/dummy/image")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
