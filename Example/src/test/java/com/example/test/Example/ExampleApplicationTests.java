package com.example.test.Example;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.boot.controller.EmployeeMangingController;
import com.example.boot.service.IEmployeeService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExampleApplicationTests {
	
	@InjectMocks
	@Autowired
	private EmployeeMangingController employeeMangingController;
	
	@Mock
	private IEmployeeService empService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	

	@Test
	public void contextLoads() {
	}

}
