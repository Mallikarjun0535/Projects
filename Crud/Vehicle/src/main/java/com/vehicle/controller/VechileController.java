package com.vehicle.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vehicle.dto.Vehicle;
import com.vehicle.service.VechicleServiceInter;

@RestController
public class VechileController {

	@Autowired
	VechicleServiceInter vechicleServiceInter;

	private static final Logger LOGGER = LoggerFactory.getLogger(VechileController.class);

	@RequestMapping(value="/vehicleDetails", method = { RequestMethod.GET }, produces={MediaType.APPLICATION_JSON_VALUE})
	public List<Vehicle> getVehicles() {
		LOGGER.info("Get Vehicles List");
		return vechicleServiceInter.getVehiclesList();
	}

	@RequestMapping(value="/saveVehicleDetails", method = { RequestMethod.POST }, consumes={MediaType.APPLICATION_JSON_VALUE})
	public String saveVehicleDetail(@RequestBody final Vehicle vehicle) {
		return vechicleServiceInter.saveVehicleDetailService(vehicle);
	}

	public String deleteVehicle() {
		return null;

	}
	
}
