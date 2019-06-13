package com.rsystems.vehiclesales.exceptions;

public final class ErrorCodes {
    //Default codes
    public static final String WARN_CODE = "5555";
    public static final String ERROR_CODE = "5000";


    //Messages
    public static final String NO_DATA = "NO DATA_AVAILABLE_FOR_REQUEST_100001";
    public static final String EXISTING_LOCK_MESSAGE = "VEHICLE_IS_ALREADY_LOCKED_BY_OTHER_USER_100002";
    public static final String VALID_DATA = "NO_DATA_FOUND_100003";
    public static final String LOCK_STATE = "FOR_PURCHASE_VEHICLE_SHOULD_BE_IN_LOCK_STATE_100004 ";
    public static final String UNABLE_TO_PRUCHASE = "WITHOUT_LOCKING_PURCHASE_SHOULD_NOT_HAPPEN_100005";
    public static final String NO_LOCK_STATE = "NO_VEHICLES_ARE_IN_LOCK_STATE_TO_OPEN_100006 ";
    public static final String NO_PROPER_DATA = "PLEASE_PROVIDE_VALID_DATA";


}
