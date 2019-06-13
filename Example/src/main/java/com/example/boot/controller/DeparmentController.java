package com.example.boot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.boot.command.Employee;
import com.example.boot.service.IEmployeeService;

@RestController
public class DeparmentController {
	
	@Autowired
	IEmployeeService eService;
	
	@RequestMapping(method = RequestMethod.GET, value = "dept/{DeptID}/pageNo/{pageNO}/limit/{limit}")
	public List<Employee> getEmpBasedOnDept(@PathVariable int DeptID,@PathVariable int pageNO , @PathVariable int limit){
		 return eService.getEmployeesBasedOnDeptID(DeptID ,pageNO, limit);
	}

}
