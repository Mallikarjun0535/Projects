package com.rsystems.vehiclesales.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice(annotations = RestController.class)
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorRespose> handleAllExceptions(Exception exception, WebRequest wRequest) {
        ErrorRespose eResponse = new ErrorRespose(new Date(), exception.getMessage(),
                wRequest.getDescription(false));

        return new ResponseEntity<>(eResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(WebappException.class)
    public final ResponseEntity<ErrorRespose> handleWebappException(WebappException webexception, WebRequest wRequest) {
        ErrorRespose eResponse = new ErrorRespose(new Date(), webexception.getMessage(),
                wRequest.getDescription(false));

        return new ResponseEntity<>(eResponse, HttpStatus.NOT_FOUND);
    }
}
