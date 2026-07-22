package org.scoula.exception;


import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Log4j2
@Order(1)
public class CommonExceptionAdvice {

    @ExceptionHandler(Exception.class)
    public String except(Exception ex , Model model){
        log.error(ex.getMessage());
        model.addAttribute("ex",ex);
        return "error_page";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFoundException(NoHandlerFoundException ex){
        return "/resources/index.html";
    }

}
