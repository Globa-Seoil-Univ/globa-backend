package org.y2k2.globa.application.survey.dto.request;

import org.y2k2.globa.common.annotation.EnumValue;
import org.y2k2.globa.common.type.SurveyType;

public record RequestSurveyDto(
        @EnumValue(enumClass = SurveyType.class, message = "설문 유형은 BSV, BAC, NEF, OBS 중 하나여야 합니다.")
        String surveyType,
        String content
) {
}