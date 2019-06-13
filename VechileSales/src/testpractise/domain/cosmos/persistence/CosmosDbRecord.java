package com.dizzion.portal.domain.cosmos.persistence;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CosmosDbRecord {
    String poolName;
    int provisionedDesktopsCount;
    LocalDate statisticsCapturingDate;
}
