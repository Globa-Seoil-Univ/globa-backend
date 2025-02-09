package org.y2k2.globa.dto.response.inquiry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.dto.common.inquiry.InquiryDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseInquiryDto {
    List<InquiryDto> inquires;
    long total;
}
