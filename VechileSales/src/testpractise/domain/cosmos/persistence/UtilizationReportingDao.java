package com.dizzion.portal.domain.cosmos.persistence;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Repository
@Slf4j
public class UtilizationReportingDao {
    private static final String SELECT_UTILIZATION_DATA = "SELECT pool_name, provisioned_dtps, timestamp " +
            "FROM desktop_counts d INNER JOIN reporting_data r ON d.id = r.id " +
            "WHERE r.cid = ? AND r.timestamp >= ? ORDER BY timestamp";

    private final JdbcTemplate jdbcTemplate;
    private final String validationQuery;

    public UtilizationReportingDao(@Qualifier("cosmosDataSource") DataSource dataSource,
                                   @Value("${spring.datasource.cosmos.validation-query}") String validationQuery) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.validationQuery = validationQuery;
    }

    public List<CosmosDbRecord> getUtilizationStatistics(String cid, LocalDate startDate) {
        try {
            return this.jdbcTemplate.query(SELECT_UTILIZATION_DATA,
                    new Object[]{cid, startDate},
                    (rs, rowNum) -> CosmosDbRecord.builder()
                            .poolName(rs.getString("pool_name"))
                            .provisionedDesktopsCount(rs.getInt("provisioned_dtps"))
                            .statisticsCapturingDate(rs.getDate("timestamp").toLocalDate())
                            .build());
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unhandled exception when trying to access COSMOS Database", e);
            return Collections.emptyList();
        }
    }

    public boolean isAvailable() {
        try {
            jdbcTemplate.execute(validationQuery);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
