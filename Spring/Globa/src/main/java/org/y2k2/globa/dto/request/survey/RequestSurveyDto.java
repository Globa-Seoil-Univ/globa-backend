package org.y2k2.globa.dto.request.survey;

import org.y2k2.globa.annotation.EnumValue;
import org.y2k2.globa.type.SurveyType;

public record RequestSurveyDto(
        @EnumValue(enumClass = SurveyType.class, message = "설문 유형은 BSV, BAC, NEF, OBS 중 하나여야 합니다.")
        String surveyType,
        String content
) {
}