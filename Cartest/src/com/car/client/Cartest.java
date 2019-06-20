package com.car.client;

import java.util.List;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Cartest implements EntryPoint {

	@Override
	public void onModuleLoad() {
		Defaults.setServiceRoot(GWT.getModuleBaseURL().replaceAll("8888", "8080"));
		//Defaults.setServiceRoot(new org.fusesource.restygwt.client.Resource( GWT.getModuleBaseURL().replaceAll("8888", "8080")).getUri());
		RootPanel rp = RootPanel.get("mallik");
		final VerticalPanel vp = new VerticalPanel();
		rp.add(vp);
		
		CarModel carModel = GWT.create(CarModel.class);
		carModel.getCars(new MethodCallback<List<CarDto>>(){

			@Override
			public void onFailure(Method method, Throwable exception) {
				throw new RuntimeException("Failure....");
			}

			@Override
			public void onSuccess(Method method, List<CarDto> response) {
					for(CarDto dto : response){
						vp.add(new Label(dto.getName()));
					}
			}
			
		});
	}
}	

	/*@Override
	public void onModuleLoad() {
		
		Defaults.setServiceRoot(GWT.getHostPageBaseURL().replaceAll("8888", "8080/Cartest"));

		final VerticalPanel vp = new VerticalPanel();
		Label lbl = new Label("Car Detail sub label");
		vp.add(lbl);
	
		
		CarModel carModel = GWT.create(CarModel.class);
		carModel.getCars(new MethodCallback<List<CarDto>>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
					GWT.log("Error");	
			}

			@Override
			public void onSuccess(Method method, List<CarDto> response) {
				for (CarDto car : response) {
					vp.add(new Label(car.getName()));
				}
			}

		});
		
		carModel.getCar("Audi", new MethodCallback<CarDto>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(Method method, CarDto response) {
			vp.add(new Label(response.getName()));
			}

		});
		
		RootPanel.get().add(vp);
	}
*/

