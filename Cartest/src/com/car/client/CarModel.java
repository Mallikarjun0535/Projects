package com.car.client;

import java.util.List;






import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("info")
public interface CarModel extends RestService {
	
	@GET
	@Path("getcarslist")
	@Produces(MediaType.APPLICATION_JSON)
	public void getCars(MethodCallback<List<CarDto>> cardtolist);
	
	
	
	
	@GET
	@Path("/car/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public void getCar(@PathParam("name") String name, MethodCallback<CarDto> dto);
	
}
