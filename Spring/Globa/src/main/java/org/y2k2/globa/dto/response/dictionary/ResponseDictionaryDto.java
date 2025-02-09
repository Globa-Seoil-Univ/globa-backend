package org.y2k2.globa.dto.response.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.dto.common.dictionary.DictionaryDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseDictionaryDto {
    List<DictionaryDto> dictionary;
}
