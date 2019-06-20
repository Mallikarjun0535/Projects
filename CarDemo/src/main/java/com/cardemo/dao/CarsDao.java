package com.cardemo.dao;
import java.util.List;

import javax.ejb.Local;

import com.cardemo.entities.Car;

@Local
public interface CarsDao {

		public void saveCar(Car car);

	    public Car findCarByName(String name);

	    public List<Car> findAll();
	
}
