package org.y2k2.globa.application.inquiry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.application.inquiry.dto.common.InquiryDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseInquiryDto {
    List<InquiryDto> inquires;
    long total;
}
