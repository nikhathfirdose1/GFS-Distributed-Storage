package com.example.client.entity.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MasterWriteResponse {
    Boolean success;
    Map<String, List<String>> data;
    @JsonIgnore
    String error;

    @JsonCreator
    public MasterWriteResponse(
            @JsonProperty("success") Boolean success,
            @JsonProperty("data") Map<String, List<String>> data
    ) {
        this.success = success;
        this.data = data;
    }

    public MasterWriteResponse(Boolean success, String error){
        this.success = success;
        this.error = error;
    }
}
