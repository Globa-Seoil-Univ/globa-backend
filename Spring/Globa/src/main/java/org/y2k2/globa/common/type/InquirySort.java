package org.y2k2.globa.common.type;

import lombok.Getter;

@Getter
public enum InquirySort {
    S,
    R,
    N;

    public static InquirySort from(String s) {
        if (s.equalsIgnoreCase("s")) {
            return InquirySort.S;
        } else if (s.equalsIgnoreCase("n")) {
            return InquirySort.N;
        } else {
            return InquirySort.R;
        }
    }
}
