package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FolderShareUserResponseDto {
    private final List<FolderShareUserDto> users;
    private Long total;
}
