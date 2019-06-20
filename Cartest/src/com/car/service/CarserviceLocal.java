package com.car.service;

import java.util.List;

import javax.ejb.Local;

import com.car.client.CarDto;


@Local
public interface CarserviceLocal {
	public String getInfo();
	
	public CarDto findCar(String name);

	public List<CarDto> fetchCars();
	
	public void saveCarInfo(CarDto carDto);
}
