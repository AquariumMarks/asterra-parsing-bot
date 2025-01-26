package org.example.asterraparsingbot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Properties {

    @JsonProperty("balloonContent")
    private String balloonContent;
}
