package com.example.boot.dto;

import java.io.Serializable;
import java.util.Date;

public class EmployeeDetail implements Serializable {
	
	
	private int empID;
	private String mngerID; 
	private double salary;
	private int age;
	private String status; 
	private Date createDate; 
	private Date updateDate;
	private int DetailId;
	
	public int getDetailId() {
		return DetailId;
	}
	public void setDetailId(int detailId) {
		DetailId = detailId;
	}
	public int getEmpID() {
		return empID;
	}
	public void setEmpID(int empID) {
		this.empID = empID;
	}
	public String getMngerID() {
		return mngerID;
	}
	public void setMngerID(String mngerID) {
		this.mngerID = mngerID;
	}
	public double getSalary() {
		return salary;
	}
	
	public EmployeeDetail() {
	
	}
	
	
	public EmployeeDetail(int empID, String mngerID, double salary, int age, String status, Date createDate,
			Date updateDate, int detailId) {
		super();
		this.empID = empID;
		this.mngerID = mngerID;
		this.salary = salary;
		this.age = age;
		this.status = status;
		this.createDate = createDate;
		this.updateDate = updateDate;
		DetailId = detailId;
	}
	public void setSalary(double salary) {
		this.salary = salary;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	
}
