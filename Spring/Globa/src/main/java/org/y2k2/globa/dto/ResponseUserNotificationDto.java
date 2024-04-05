package org.y2k2.globa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResponseUserNotificationDto implements Serializable {
    private Boolean uploadNofi;
    private Boolean shareNofi;
    private Boolean eventNofi;
}