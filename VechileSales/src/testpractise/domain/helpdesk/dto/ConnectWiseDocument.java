package com.dizzion.portal.domain.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ConnectWiseDocument {
    long id;
    Info _info;

    public static class Info {
        @JsonProperty("filename")
        String filename;
    }

    public String getFileName() {
        return _info.filename;
    }
}
