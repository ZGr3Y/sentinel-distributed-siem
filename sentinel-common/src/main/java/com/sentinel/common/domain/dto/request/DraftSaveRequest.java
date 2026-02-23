package com.sentinel.common.domain.dto.request;

public class DraftSaveRequest {
    private String payload;

    public DraftSaveRequest() {
    }

    public DraftSaveRequest(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
