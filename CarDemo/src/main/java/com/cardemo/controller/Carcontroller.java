package com.cardemo.controller;

import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.cardemo.dto.CarDto;
import com.cardemo.service.CarserviceLocal;

@Path("info")
public class Carcontroller {

	@EJB
	private CarserviceLocal carservice;

	@GET
	@Path("/name")
	@Produces(MediaType.TEXT_PLAIN)
	public String getName() {
		return carservice.getInfo();
	}
	
	@GET
	@Path("/getcar/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public CarDto GetCarbyName(@PathParam("name") final String name) {
		return carservice.findCar(name);
	}
	
	@GET
	@Path("/getcarslist")
	@Produces(MediaType.APPLICATION_JSON)
	public List<CarDto> getCarsList() {
	return carservice.fetchCars();
	}

	@POST
	@Path("/savecar")
	@Consumes(MediaType.APPLICATION_JSON)
	public void saveCar(final CarDto carDto) {
		carservice.saveCarInfo(carDto);
	}

}
