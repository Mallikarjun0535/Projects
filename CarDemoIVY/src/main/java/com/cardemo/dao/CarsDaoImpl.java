package com.cardemo.dao;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.cardemo.entities.Car;

@Stateless
@LocalBean
public class CarsDaoImpl implements CarsDao {
	

	 @PersistenceContext(unitName = "testPU")
	EntityManager em;

	@Override
	public void saveCar(Car car) {
	        em.persist(car);
	        em.flush();
	}

	@Override
	public Car findCarByName(String name) {

		Query query = em.createNamedQuery("Car.findByName");

		query.setParameter("name", name);
		Car car = (Car) query.getSingleResult();

		return car;
	}

	@Override
	public List<Car> findAll() {

		Query query = em.createNamedQuery("Car.findAll");

		List<Car> cars = query.getResultList();

		return cars;
	}

}
