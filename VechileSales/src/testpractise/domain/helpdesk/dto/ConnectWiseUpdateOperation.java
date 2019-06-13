package com.dizzion.portal.domain.helpdesk.dto;

import lombok.Value;

import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseUpdateOperation.Operation.REMOVE;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseUpdateOperation.Operation.REPLACE;

@Value
public class ConnectWiseUpdateOperation {
    Operation op;
    String path;
    Object value;

    public static ConnectWiseUpdateOperation replaceOp(String path, Object value) {
        return new ConnectWiseUpdateOperation(REPLACE, path, value);
    }

    public static ConnectWiseUpdateOperation removeOp(String path) {
        return new ConnectWiseUpdateOperation(REMOVE, path, null);
    }

    enum Operation {
        ADD,
        REPLACE,
        REMOVE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
