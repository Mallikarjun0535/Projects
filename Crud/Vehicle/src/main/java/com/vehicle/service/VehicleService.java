package com.vehicle.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vehicle.dao.VehicleRepoIntfce;
import com.vehicle.domain.VehicleEntity;
import com.vehicle.dto.Vehicle;


@Service
public class VehicleService implements VechicleServiceInter {

	@Autowired
	private VehicleRepoIntfce vehicleRepoIntfce;

	@Override
	public String saveVehicleDetailService(Vehicle vehicle) {
		VehicleEntity vEntity = new VehicleEntity();
		vEntity.setVehiclename(vehicle.getVehiclename());
		vEntity.setManfaturingSite(vehicle.getManfaturingSite());
		vEntity.setStatus("A");
		vEntity.setAlias(vehicle.getAlias());
		vEntity.setCreateDate(new Date());
		vehicleRepoIntfce.saveVehicleDetailDao(vEntity);
		return "success";
	}

	@Override
	public List<Vehicle> getVehiclesList() {
		List<Vehicle> vechileslist = new ArrayList<>();
		List<VehicleEntity> ventityList = vehicleRepoIntfce.getVehiclesList();

		if (!CollectionUtils.isEmpty(ventityList)) {
			vechileslist = ventityList.stream().map(entity -> {
				Vehicle vehicle = new Vehicle();
				vehicle.setVehicleId(entity.getVehicleId());
				vehicle.setVehiclename(entity.getVehiclename());
				vehicle.setStatus(entity.getStatus());
				vehicle.setAlias(entity.getAlias());
				vehicle.setManfaturingSite(entity.getManfaturingSite());
				vehicle.setCreateDate(entity.getCreateDate());
				return vehicle;
			}).collect(Collectors.toList());
		}
		return vechileslist;
	}

}
