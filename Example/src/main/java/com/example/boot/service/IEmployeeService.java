package com.example.boot.service;

import java.util.List;

import com.example.boot.command.Employee;

public interface IEmployeeService {

	public List<Employee> getEmployees();
	
	public List<Employee> getEmployeesBasedOnDeptID(int id, int pageNo, int limit);
	
	public Employee getEmployee(int id);

	public String addEmployeeToList(Employee emp);
	

}
