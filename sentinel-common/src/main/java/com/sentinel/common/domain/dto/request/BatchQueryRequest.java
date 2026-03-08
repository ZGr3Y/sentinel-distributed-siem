package com.sentinel.common.domain.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchQueryRequest {

    private List<String> ipsToInvestigate;
}
