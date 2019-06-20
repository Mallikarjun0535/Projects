package com.vehicle.service;

import java.util.List;

import com.vehicle.dto.Vehicle;


public interface VechicleServiceInter {

 public String saveVehicleDetailService(Vehicle vehicle);
 
 public List<Vehicle>  getVehiclesList();
	
}
