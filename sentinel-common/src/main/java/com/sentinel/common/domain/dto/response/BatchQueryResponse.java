package com.sentinel.common.domain.dto.response;

import com.sentinel.common.domain.entity.Alert;
import java.util.List;
import java.util.Map;

public class BatchQueryResponse {

    private Map<String, List<Alert>> ipAlertsMap;
    private int totalIpsQueried;

    public BatchQueryResponse() {
    }

    public BatchQueryResponse(Map<String, List<Alert>> ipAlertsMap, int totalIpsQueried) {
        this.ipAlertsMap = ipAlertsMap;
        this.totalIpsQueried = totalIpsQueried;
    }

    public Map<String, List<Alert>> getIpAlertsMap() {
        return ipAlertsMap;
    }

    public void setIpAlertsMap(Map<String, List<Alert>> ipAlertsMap) {
        this.ipAlertsMap = ipAlertsMap;
    }

    public int getTotalIpsQueried() {
        return totalIpsQueried;
    }

    public void setTotalIpsQueried(int totalIpsQueried) {
        this.totalIpsQueried = totalIpsQueried;
    }
}
