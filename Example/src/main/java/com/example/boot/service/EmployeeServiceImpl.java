package com.example.boot.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.boot.command.Employee;
import com.example.boot.dao.IDepartment;
import com.example.boot.dao.IEmployeeDao;
import com.example.boot.dto.EmployeeDto;

@Service
public class EmployeeServiceImpl implements IEmployeeService {
	@Autowired
	private IEmployeeDao empDao;

	@Autowired
	private IDepartment idepart;

	/*
	 * @see getEmployees() mainly for Fetching the Employees list
	 */
	public List<Employee> getEmployees() {
		List<EmployeeDto> dtolist = empDao.findAll();
		if(CollectionUtils.isEmpty(dtolist)) {
			return Collections.emptyList();
		}
		return	dtolist.stream().map(EmployeeServiceImpl :: toEmployee).collect(Collectors.toList());
	}

	private static Employee toEmployee(EmployeeDto edto) {
		Employee emp = new Employee();
		emp.setEmpId(edto.getEmpID());
		emp.setFirstName(edto.getEmpFirstName());
		emp.setLastName(edto.getEmpLastName());
		emp.setGender(edto.getGender());
		emp.setNationality(edto.getNationality());
		emp.setCreateDate(edto.getDateOfjoin());
		return emp;
	}

	/*
	 * @see addEmployeeToList adding employee to table
	 */
	public String addEmployeeToList(Employee emp) {
		EmployeeDto edto = new EmployeeDto();
		
		if (null != emp && !emp.equals("")) {

			edto.setEmpFirstName(emp.getFirstName());
			edto.setEmpLastName(emp.getLastName());
			edto.setNationality(emp.getNationality());
			edto.setDateOfjoin(emp.getCreateDate());
			edto.setGender(emp.getGender());
			empDao.insert(edto);

			return "sucess";
		} else {

			return "Failure";
		}
	}

	/*
	 * @see getEmployee(int) fetching the employee based on id
	 */
	@Override
	public Employee getEmployee(int id) {

		Employee emp = new Employee();
		EmployeeDto dto = empDao.find(id);

		if (n  !dto.equals("")) {
			emp.setEmpId(dto.getEmpID());
			emp.setFirstName(dto.getEmpFirstName());
			emp.setLastName(dto.getEmpLastName());
			emp.setGender(dto.getGender());
			emp.setNationality(dto.getNationality());
			emp.setCreateDate(dto.getDateOfjoin());
		}

		return emp;
	}

	@Override
	public List<Employee> getEmployeesBasedOnDeptID(int id, int pageNo, int limit) {
		// ,pageNo*limit,limit)
		List<Employee> volist = new ArrayList<>();
		RowBounds row = new RowBounds(pageNo * limit, limit);
		List<EmployeeDto> dtolist = idepart.getEmployeesforDeptID(id, row);

		if (null != dtolist && !dtolist.isEmpty()) {
			for (EmployeeDto edto : dtolist) {
				Employee emp = new Employee();
				emp.setEmpId(edto.getEmpID());
				emp.setFirstName(edto.getEmpFirstName());
				emp.setLastName(edto.getEmpLastName());
				emp.setGender(edto.getGender());
				emp.setNationality(edto.getNationality());
				emp.setCreateDate(edto.getDateOfjoin());
				volist.add(emp);
			}
		}
		return volist;

	}

	/*
	 * //System.out.println(""+id+"dddddd"+pageNo+"ddddd"+limit); //RowBounds row =
	 * new RowBounds(pageNo*limit,limit); DepartmentDo dtolist =
	 * idepart.getEmployee(id); List<EmployeeDto> edtos = dtolist.getEdto();
	 * List<Employee> volist = new ArrayList<>(); for (EmployeeDto edto : edtos) {
	 * Employee emp = new Employee(); emp.setEmpId(edto.getEmpID());
	 * emp.setFirstName(edto.getEmpFirstName());
	 * emp.setLastName(edto.getEmpLastName()); emp.setGender(edto.getGender());
	 * emp.setNationality(edto.getNationality());
	 * emp.setCreateDate(edto.getDateOfjoin()); volist.add(emp); }
	 * 
	 * return volist;
	 */

}
