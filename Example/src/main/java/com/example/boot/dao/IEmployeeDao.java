package com.example.boot.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.boot.dto.EmployeeDetail;
import com.example.boot.dto.EmployeeDto;

@Mapper
public interface IEmployeeDao {
	// Fetching the Employee List
	@Select("select EMP_ID,EMP_FIRST_NAME,EMP_LAST_NAME,GENDER,NATIONALITY,DATE_OF_JOIN,UPDATE_DATE "
			+ "from Employee ")
	@Results({ @Result(property = "empID", column = "EMP_ID"),
			@Result(property = "empFirstName", column = "EMP_FIRST_NAME"),
			@Result(property = "empLastName", column = "EMP_LAST_NAME"),
			@Result(property = "gender", column = "GENDER"),
			@Result(property = "nationality", column = "NATIONALITY"),
			@Result(property = "dateOfjoin", column = "DATE_OF_JOIN"),
			@Result(property = "updateDate", column = "UPDATE_DATE"),
			@Result(property = "edetail", javaType = EmployeeDetail.class, column = "EMP_ID", 
			one = @One(select = "com.example.boot.dao.IEmployeeDao.findEmployeeDetail")) })
	List<EmployeeDto> findAll();

	// For Fetching the Employee Details
	@Select("select AGE,SALARY,EMP_STATUS from EMPLOYEE_DETAILS where EMP_ID = #{id} ")
	@Results({ @Result(property = "empID", column = "EMP_ID"), @Result(property = "age", column = "AGE"),
			@Result(property = "status", column = "EMP_STATUS"), @Result(property = "salary", column = "SALARY") })
	EmployeeDetail findEmployeeDetail(int id);

	// FETCHING THE EMPLOYEE DETAILS BY ID
	@Select("select EMP_ID,EMP_FIRST_NAME,EMP_LAST_NAME,GENDER,NATIONALITY,DATE_OF_JOIN,UPDATE_DATE"
			+ " from Employee where EMP_ID = #{empID}")
	@Results({ @Result(property = "empID", column = "EMP_ID"),
			@Result(property = "empFirstName", column = "EMP_FIRST_NAME"),
			@Result(property = "empLastName", column = "EMP_LAST_NAME"),
			@Result(property = "gender", column = "GENDER"),
			@Result(property = "nationality", column = "NATIONALITY"),
			@Result(property = "dateOfjoin", column = "DATE_OF_JOIN"),
			@Result(property = "updateDate", column = "UPDATE_DATE") })
	EmployeeDto find(int id);

	// INSERTING THE EMPLOYEE OBJECT IN TO DATABASE
	@Insert("INSERT INTO Employee(EMP_FIRST_NAME,EMP_LAST_NAME,GENDER,NATIONALITY,DATE_OF_JOIN)"
			+ " VALUES(#{empFirstName}, #{empLastName}, #{gender}, #{nationality}, #{dateOfjoin})")
	void insert(EmployeeDto user);

	/*
	 * @Update("UPDATE users SET userName=#{userName},nick_name=#{nickName} WHERE id =#{id}"
	 * ) void update(EmployeeDto user);
	 * 
	 * @Delete("DELETE FROM users WHERE id =#{id}") void delete(Long id);
	 */

}