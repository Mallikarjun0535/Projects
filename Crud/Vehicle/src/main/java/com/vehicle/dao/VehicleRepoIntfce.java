package com.vehicle.dao;

import java.util.List;

import org.mybatis.spring.annotation.MapperScan;

import com.vehicle.domain.VehicleEntity;


public interface VehicleRepoIntfce {
	
	public void  saveVehicleDetailDao(VehicleEntity vehicleEntity);
	
	public List<VehicleEntity>  getVehiclesList();

}
