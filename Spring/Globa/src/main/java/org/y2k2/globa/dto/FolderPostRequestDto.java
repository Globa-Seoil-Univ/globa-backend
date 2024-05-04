package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FolderPostRequestDto {
    private String title;
    private List<ShareTarget> shareTarget;
}
