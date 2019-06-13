package com.example.boot.dto;

import java.util.Date;

public class DeptEmployee {
	
	private int empID;
	private int deptId;
	private int status; 
	private Date createDate; 
	private Date updateDate; 
	
	public int getEmpID() {
		return empID;
	}
	public void setEmpID(int empID) {
		this.empID = empID;
	}
	public int getDeptId() {
		return deptId;
	}
	public void setDeptId(int deptId) {
		this.deptId = deptId;
	}

	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
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
	
	
	@Override
	public String toString() {
		return "DeptEmployee [empID=" + empID + ", deptId=" + deptId + ", status=" + status + ", createDate="
				+ createDate + ", updateDate=" + updateDate + "]";
	}
}
