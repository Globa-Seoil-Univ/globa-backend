package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DictionaryDto {
    private String word;
    private String engWord;
    private String description;
    private String category;
    private String pronunciation;
}