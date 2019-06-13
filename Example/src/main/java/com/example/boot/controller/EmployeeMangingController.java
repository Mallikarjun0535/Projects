package com.example.boot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.boot.command.Employee;
import com.example.boot.service.IEmployeeService;

@RestController
public class EmployeeMangingController {

	@Autowired
	private IEmployeeService empService;

	@RequestMapping(method = RequestMethod.GET, value = "/list")
	public List<Employee> getEmployeeList() {
		
		return empService.getEmployees();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/list/{id}")
	public Employee getEmployeeList(@PathVariable int id) {
		return empService.getEmployee(id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/add")
	public String addEmployee(@RequestBody Employee employee) {
		return empService.addEmployeeToList(employee);
	}

}
