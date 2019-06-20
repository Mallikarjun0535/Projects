package com.car.client;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class CarDto {
	private int Id;
	private String name;
	private int price;
	
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	
	@JsonCreator
	public CarDto(@JsonProperty("id") int id, @JsonProperty("name") String name, @JsonProperty("price") int price) {
		super();
		Id = id;
		this.name = name;
		this.price = price;
	}
	
	public CarDto() {
		
	}

}
