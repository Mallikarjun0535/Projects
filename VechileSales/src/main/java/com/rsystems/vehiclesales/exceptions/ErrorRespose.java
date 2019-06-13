package com.rsystems.vehiclesales.exceptions;

import java.util.Date;

public class ErrorRespose {

    private Date time;
    private String message;
    private String Details;
    public ErrorRespose(Date time, String message, String details) {
        super();
        this.time = time;
        this.message = message;
        Details = details;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return Details;
    }

    public void setDetails(String details) {
        Details = details;
    }


    @Override
    public String toString() {
        return "ErrorRespose [time=" + time + ", message=" + message + ", Details=" + Details + "]";
    }
}
