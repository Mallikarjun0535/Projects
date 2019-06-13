package com.dizzion.portal.domain.user.dto;

import lombok.Value;

@Value
public class BatchUserCreateCsvRecord {
    String firstName;
    String lastName;
    String email;
    String organization;
    String role;
    String mobilePhone;
    String workPhone;
}
