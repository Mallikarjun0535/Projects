package com.car.dao;
import java.util.List;

import javax.ejb.Local;

import com.car.entities.Car;

@Local
public interface CarsDao {

		public void saveCar(Car car);

	    public Car findCarByName(String name);

	    public List<Car> findAll();
}
