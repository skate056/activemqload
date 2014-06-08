package org.apache.activemq;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class DummyLoad {

    @JsonProperty
    private UUID uuid = UUID.randomUUID();

    @JsonProperty
    private String payload = "default load";

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
