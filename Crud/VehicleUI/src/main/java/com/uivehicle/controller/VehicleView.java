package com.uivehicle.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.uivehicle.exceptions.VehicleException;
import com.uivehicle.model.Vehicle;

@Controller
@RequestMapping("/test")
public class VehicleView {

	@Value("${url.vc.list}")
	private String allvehicleUrl;

	@Autowired
	RestTemplate restTemplate;

	@RequestMapping(value = "/vehicle/all", method = { RequestMethod.GET })
	public String getAllVehicles(Model model) {
		try {
			Vehicle[] vehicles = restTemplate.getForObject(allvehicleUrl, Vehicle[].class);
			model.addAttribute("vehiclelist", Arrays.asList(vehicles));
			return "vehiclelist";
		} catch (RestClientException e) {
			throw new VehicleException(e);
		}
	}

	@RequestMapping(value = "/saveVehicle", method = { RequestMethod.POST })
	public Vehicle saveVehicle(@RequestBody Vehicle vehicle) {
		return restTemplate.postForObject("http://localhost:8080/Vehicle/saveVehicleDetails", vehicle, Vehicle.class);
	}

	@RequestMapping(value = "/name" , method = { RequestMethod.GET })
	public String getName() {
		return "Mallik";
	}

}
