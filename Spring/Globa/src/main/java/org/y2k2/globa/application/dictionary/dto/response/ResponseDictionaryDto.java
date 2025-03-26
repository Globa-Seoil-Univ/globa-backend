package org.y2k2.globa.application.dictionary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.application.dictionary.dto.common.DictionaryDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseDictionaryDto {
    List<DictionaryDto> dictionary;
}
