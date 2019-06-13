package com.example.boot.dto;

import java.io.Serializable;
import java.util.Date;

public class EmployeeDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int empID;
	private String empFirstName;
	private String 	empLastName;
	private String gender;
	private String nationality;
	private Date dateOfjoin;
	private Date updateDate;
	private EmployeeDetail edetail;

	public EmployeeDetail getEdetail() {
		return edetail;
	}
	public void setEdetail(EmployeeDetail edetail) {
		this.edetail = edetail;
	}
	public int getEmpID() {
		return empID;
	}
	public void setEmpID(int empID) {
		this.empID = empID;
	}
	public String getEmpFirstName() {
		return empFirstName;
	}
	public void setEmpFirstName(String empFirstName) {
		this.empFirstName = empFirstName;
	}
	public String getEmpLastName() {
		return empLastName;
	}
	public void setEmpLastName(String empLastName) {
		this.empLastName = empLastName;
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
	public Date getDateOfjoin() {
		return dateOfjoin;
	}
	public void setDateOfjoin(Date dateOfjoin) {
		this.dateOfjoin = dateOfjoin;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

}
