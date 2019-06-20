package com.car.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

import org.apache.commons.collections.CollectionUtils;

import com.car.client.CarDto;
import com.car.dao.CarsDao;
import com.car.entities.Car;


/**
 * Session Bean implementation class Carservice
 */
@Stateless
@LocalBean
public class Carservice implements CarserviceLocal {
	
	@EJB
	private CarsDao carsdao;
	

    public Carservice() {
    }

	@Override
	public String getInfo() {
		return "MallikAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	}
    
	
	@Override
	public CarDto findCar(String name) {
		Car car = carsdao.findCarByName(name);
		CarDto carDto = new CarDto();
		carDto.setId(car.getId());
		carDto.setName(car.getName());
		carDto.setPrice(car.getPrice());
		return carDto;
	}

	@Override
	public List<CarDto> fetchCars() {
		List<CarDto> list = new ArrayList<>();
		List<Car> cars = carsdao.findAll();
			list = cars.stream().map(entity -> {
				CarDto car = new CarDto();
				car.setId(entity.getId());
				car.setName(entity.getName());
				car.setPrice(entity.getPrice());
				return car;
			}).collect(Collectors.toList());
	
		return list;
	}

	@Override
	@TransactionAttribute
	public void saveCarInfo(CarDto carDto) {
		if(!Objects.isNull(carDto)) {
			Car c = new Car();
			c.setId(carDto.getId());
			c.setName(carDto.getName());
			c.setPrice(carDto.getPrice());
			carsdao.saveCar(c);
		}
	}

}
