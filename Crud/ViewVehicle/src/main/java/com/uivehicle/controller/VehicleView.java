package com.uivehicle.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.uivehicle.model.Vehicle;
import com.uivehicle.model.VehicleException;

@RestController
@RequestMapping("/test")
public class VehicleView {
	
	
	@Value("${url.vc.list}")
    private String allvehicleUrl;
	
	
	@Autowired
	RestTemplate restTemplate;
	

	@RequestMapping(value="/vehicle/all", method = { RequestMethod.GET } , produces={MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public List<Vehicle> getAllVehicles() {
        try {
        	Vehicle[] varray = restTemplate.getForObject("http://localhost:8080/Vehicle/vehicleDetails", Vehicle[].class);
        	return Arrays.asList(varray);
        }catch(RestClientException e){
         throw new VehicleException(e);
        }
    }
	
	
	@RequestMapping(value="/name")
	public String getName() {
		return "Mallik";
	}
	
	
}


