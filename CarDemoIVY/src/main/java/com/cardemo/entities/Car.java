package com.cardemo.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the cars database table.
 * 
 */
@Entity
@Table(name = "cars")
@NamedQueries({
		@NamedQuery(name = "Car.findAll", query = "SELECT c FROM Car c"),
		@NamedQuery(name = "Car.findByName", query = "SELECT c FROM Car c WHERE c.name = :name") })
public class Car implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private String name;

	private int price;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Car() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrice() {
		return this.price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

}