package com.rsystems.vehiclesales.services;

import com.rsystems.vehiclesales.command.Vehicles;

import java.util.List;

public interface VehiclesDemoServiceI {

    List<Vehicles> getDemovechiclesList(String vendor);

    String lockForVehicle(Vehicles vehicle) throws Exception;

    String purchaseVehicle(Vehicles vehicle) throws Exception;

    String releaseLock(Vehicles vehicle) throws Exception;

}
