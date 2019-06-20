package com.uivehicle.model;

public final class VehicleException extends RuntimeException {
	private static final long serialVersionUID = -3032427340076346861L;
	
	public VehicleException(String message){
		super(message);
	}
	
	public VehicleException(Throwable tr){
		super(tr);
	}
}
