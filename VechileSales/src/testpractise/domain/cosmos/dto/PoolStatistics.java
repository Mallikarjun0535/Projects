package com.dizzion.portal.domain.cosmos.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PoolStatistics {
    String poolName;
    List<DailyPoolStatistics> dailyPoolStatistics;
}