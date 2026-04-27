package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record IntervalsUpdateProfileDTO(
        @JsonProperty("weight") Double weight,

        @JsonProperty("height") Double height,

        @JsonProperty("icu_date_of_birth")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate fechaNacimiento,

        @JsonProperty("sex") String sex,
        @JsonProperty("icu_resting_hr")
        Integer fcReposo,
        @JsonProperty("sportSettings")
        List<IntervalsSportSettingDTO> sportSettings
) {
}