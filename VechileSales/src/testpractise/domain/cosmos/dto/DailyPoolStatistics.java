package com.dizzion.portal.domain.cosmos.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class DailyPoolStatistics {
    int provisionedDesktopsCount;
    LocalDate statisticsCapturingDate;
}
