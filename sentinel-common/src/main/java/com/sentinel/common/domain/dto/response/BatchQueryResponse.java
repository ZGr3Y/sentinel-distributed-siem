package com.sentinel.common.domain.dto.response;

import com.sentinel.common.domain.entity.Alert;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchQueryResponse {

    private Map<String, List<Alert>> ipAlertsMap;
    private int totalIpsQueried;
}
