package com.cardemo.dto;

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
	
	public CarDto(int id, String name, int price) {
		super();
		Id = id;
		this.name = name;
		this.price = price;
	}
	
	public CarDto() {
		
	}

}
