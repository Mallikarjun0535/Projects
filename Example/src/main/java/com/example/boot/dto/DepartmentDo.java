package com.example.boot.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class DepartmentDo implements Serializable{
	
	private int deptId;
	private String deptname;
	private String status;
	private Date createDate; 
	private Date updateDate;
	public List<EmployeeDto> getEdto() {
		return edto;
	}
	public void setEdto(List<EmployeeDto> edto) {
		this.edto = edto;
	}
	private List<EmployeeDto> edto;
	
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
	public int getDeptId() {
		return deptId;
	}
	public void setDeptId(int deptId) {
		this.deptId = deptId;
	}
	public String getDeptname() {
		return deptname;
	}
	public void setDeptname(String deptname) {
		this.deptname = deptname;
	}
	
	

}
