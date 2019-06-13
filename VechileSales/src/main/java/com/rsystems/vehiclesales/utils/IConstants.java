package com.rsystems.vehiclesales.utils;

public class IConstants {

    public static final String ORDER_SUCESS = "YOU_PURCHASED_VEHICLE_SUCCESSFULLY";
    public static final String UNLOCK_VEHICLE = "VEHICLE_UNLOCKED_SUCCESSFULLY";
    public static final String VEHICLE_LOCKED = "VEHICLE_IS_SUCCESSFULLY_LOCKED_BY_YOU";
    public static final String VEHICLE_LOCKED_BY_OTHER = "VEHICLE_IS_ALREADY_LOCKED_BY_OTHER_USER";

    public enum Vehilce_Status {
        LOCK, OPEN, PURCHASED;
    }

}
