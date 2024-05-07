package org.y2k2.globa.dto;

public enum SortDto {
    S("s"),
    R("r"),
    N("n");

    private final String value;

    SortDto(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static SortDto valueOfString(String s) {
        if (s.equals("s")) {
            return SortDto.S;
        } else if (s.equals("n")) {
            return SortDto.N;
        } else {
            return SortDto.R;
        }
    }
}
