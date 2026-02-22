package com.sentinel.common.domain.dto.request;

import java.util.List;

public class BatchQueryRequest {

    private List<String> ipsToInvestigate;

    public BatchQueryRequest() {
    }

    public BatchQueryRequest(List<String> ipsToInvestigate) {
        this.ipsToInvestigate = ipsToInvestigate;
    }

    public List<String> getIpsToInvestigate() {
        return ipsToInvestigate;
    }

    public void setIpsToInvestigate(List<String> ipsToInvestigate) {
        this.ipsToInvestigate = ipsToInvestigate;
    }
}
