package com.alvaro.enduremetrics.dto.intervals;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record IntervalsUpdateProfileDTO(
        @JsonProperty("weight") Double weight,

        @JsonProperty("height") Double height,

        @JsonProperty("icu_date_of_birth")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate fechaNacimiento,

        @JsonProperty("sex") String sex
) {}