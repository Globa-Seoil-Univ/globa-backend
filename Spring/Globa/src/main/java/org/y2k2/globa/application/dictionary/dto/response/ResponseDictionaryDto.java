package org.y2k2.globa.application.dictionary.dto.response;

import org.y2k2.globa.application.dictionary.dto.common.DictionaryDto;

import java.util.List;

public record ResponseDictionaryDto(
        List<DictionaryDto> dictionary
) {
}
