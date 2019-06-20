package com.vehicle.dto;

import java.util.Date;

public class Vehicle {
	
	private Integer vehicleId;
	private String vehiclename;
	private String alias;
	private String manfaturingSite;
	private String status;
	private Date createDate;
	private Date updateDate;

	public Integer getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(Integer vehicleId) {
		this.vehicleId = vehicleId;
	}
	public String getVehiclename() {
		return vehiclename;
	}
	public void setVehiclename(String vehiclename) {
		this.vehiclename = vehiclename;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getManfaturingSite() {
		return manfaturingSite;
	}
	public void setManfaturingSite(String manfaturingSite) {
		this.manfaturingSite = manfaturingSite;
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
