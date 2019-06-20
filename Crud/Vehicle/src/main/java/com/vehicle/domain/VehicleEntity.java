package com.vehicle.domain;

import java.io.Serializable;
import java.util.Date;

public class VehicleEntity  implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Integer vehicleId;
	private String vehiclename;
	private String manfaturingSite;
	private String status;
	private String alias;
	private Date createDate;
	private Date updateDate;
	
	
	
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
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
