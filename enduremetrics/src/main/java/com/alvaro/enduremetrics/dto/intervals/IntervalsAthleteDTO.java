package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntervalsAthleteDTO(
        String id,
        String firstname,
        @JsonProperty("icu_weight") Double peso,
        @JsonProperty("icu_resting_hr") Integer fcReposo,
        List<IntervalsSportSettingDTO> sportSettings // Jackson mapeará la lista automáticamente
) {}