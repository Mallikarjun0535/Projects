package com.rsystems.vehiclesales.controller;

import com.rsystems.vehiclesales.command.Vehicles;
import com.rsystems.vehiclesales.services.VehiclesDemoServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Vehicles")
public class VehiclesDemoController {
	
	
	@Autowired
	private VehiclesDemoServiceI vechiclesDemoService;
	
	
	@GetMapping(value = "/list/{vendor}" , produces = {MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody ResponseEntity<?> listVehicles(@PathVariable()  final String vendor) {
		List<Vehicles> vehiclesList = vechiclesDemoService.getDemovechiclesList(vendor);
		return new ResponseEntity<>(vehiclesList, HttpStatus.OK);
	}
	
	@PutMapping(value = "/lockVehicle" , produces = {MediaType.APPLICATION_JSON_VALUE} , consumes = {MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody ResponseEntity<?> lockVehicleForPurchase(@RequestBody final Vehicles vehicle) throws Exception{
		return new ResponseEntity<>(vechiclesDemoService.lockForVehicle(vehicle), HttpStatus.OK);
	}
	
	@PutMapping(value = "/releaseLock" , produces = {MediaType.APPLICATION_JSON_VALUE} , consumes = {MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody ResponseEntity<?> releaseLockOnVehicle(@RequestBody final Vehicles vehicle) throws  Exception{
		return new ResponseEntity<>(vechiclesDemoService.releaseLock(vehicle), HttpStatus.OK);
	}

	@PutMapping(value = "/purchaseVehicle" , produces = {MediaType.APPLICATION_JSON_VALUE} , consumes = {MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody ResponseEntity<?> VehicleForPurchase(@RequestBody final Vehicles vehicle) throws  Exception{
		return new ResponseEntity<>(vechiclesDemoService.purchaseVehicle(vehicle), HttpStatus.OK);
	}
	
}

