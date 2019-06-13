package com.dizzion.portal.config;

import com.dizzion.portal.domain.user.dto.BatchUserCreateCsvRecord;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvConfig {

    @Bean
    @Qualifier("csvUserReader")
    public ObjectReader csvUserReader() {
        CsvMapper mapper = new CsvMapper();
        return mapper.readerFor(BatchUserCreateCsvRecord.class).with(CsvSchema.builder()
                .addColumn("firstName")
                .addColumn("lastName")
                .addColumn("email")
                .addColumn("organization")
                .addColumn("role")
                .addColumn("mobilePhone")
                .addColumn("workPhone")
                .build()
        );
    }
}
