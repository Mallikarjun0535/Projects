package com.dizzion.portal.domain.maintenance.dto;

import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventApprovalEntity;
import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import lombok.Value;

@Value
public class MaintenanceEventApproval {
    ShortUserInfo user;
    boolean approved;

    public static MaintenanceEventApproval from(MaintenanceEventApprovalEntity entity) {
        return new MaintenanceEventApproval(ShortUserInfo.from(entity.getUser()), entity.isApproved());
    }
}
