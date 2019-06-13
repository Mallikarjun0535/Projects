package com.example.boot.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.RowBounds;

import com.example.boot.dto.EmployeeDto;

@Mapper
public interface IDepartment {
	
	@Select("SELECT E.EMP_ID,E.EMP_FIRST_NAME FROM EMPLOYEE E "
			+ "INNER JOIN EMPLOYEE_DEPARTMENT ED ON E.EMP_ID = ED.EMP_ID "
			+ "INNER JOIN DEPARTMENT D ON D.DEPT_ID = ED.DEPT_ID where ED.DEPT_ID = #{id}")
	@Options(resultSetType=ResultSetType.SCROLL_INSENSITIVE)
	@Results({ 
		@Result(property = "empID", column = "EMP_ID"),
		@Result(property = "empFirstName", column = "EMP_FIRST_NAME"),
		@Result(property = "empLastName", column = "EMP_LAST_NAME"),
		@Result(property = "gender", column = "GENDER"),
		@Result(property = "nationality", column = "NATIONALITY"),
		@Result(property = "dateOfjoin", column = "DATE_OF_JOIN"),
		@Result(property = "updateDate", column = "UPDATE_DATE") })
	public List<EmployeeDto> getEmployeesforDeptID(int id, RowBounds row);
	
	
/*	@Select("SELECT DEPT_ID,DEPT_NAME,STATUS,CREATE_DATE from Department where DEPT_ID = #{id}")
	@Results({ 
		@Result(property = "deptId", column = "DEPT_ID"),
		@Result(property = "deptname", column = "DEPT_NAME"),
		@Result(property = "status", column = "STATUS"),
		@Result(property = "createDate", column = "CREATE_DATE"),
		@Result(property = "EmployeeDto", javaType=List.class, column="DEPT_ID",
         many=@Many(select="com.example.boot.dao.IDepartment.getEmployeesforDeptID")
		) })
	public DepartmentDo getEmployee(int id);
	
	
	@Select("SELECT E.EMP_FIRST_NAME,E.EMP_ID FROM EMPLOYEE E "
			+ "INNER JOIN EMPLOYEE_DEPARTMENT ED ON E.EMP_ID = ED.EMP_ID where ED.DEPT_ID = #{id}")
	public List<EmployeeDto> getEmployeesforDeptID(int id);*/

}

