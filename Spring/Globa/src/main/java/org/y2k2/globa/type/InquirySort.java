package org.y2k2.globa.type;

import lombok.Getter;

@Getter
public enum InquirySort {
    S("s"),
    R("r"),
    N("n");

    private final String value;

    InquirySort(String value) {
        this.value = value;
    }

    public static InquirySort valueOfString(String s) {
        if (s.equals("s")) {
            return InquirySort.S;
        } else if (s.equals("n")) {
            return InquirySort.N;
        } else {
            return InquirySort.R;
        }
    }
}
