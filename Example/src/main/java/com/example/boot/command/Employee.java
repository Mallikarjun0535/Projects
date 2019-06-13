package com.example.boot.command;

import java.util.Date;

import com.example.boot.dto.EmployeeDetail;

public class Employee {
	
	private int empId;
	private String firstName;
	private int baseEmpId;
	private String gender;
	private String nationality;
	private String lastName;
	private Date createDate;
	private Date updateDate;

	public int getEmpId() {
		return empId;
	}
	public void setEmpId(int empId) {
		this.empId = empId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public int getBaseEmpId() {
		return baseEmpId;
	}
	public void setBaseEmpId(int baseEmpId) {
		this.baseEmpId = baseEmpId;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getNationality() {
		return nationality;
	}
	public void setNationality(String nationality) {
		this.nationality = nationality;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
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
