package com.harsh.rentify.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.name}")
    private String appName;

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return buildErrorView("error/404", HttpStatus.NOT_FOUND, "Page not found", exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusiness(BusinessException exception, HttpServletRequest request) {
        return buildErrorView("error/400", HttpStatus.BAD_REQUEST, "Request could not be completed", exception.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        return buildErrorView("error/403", HttpStatus.FORBIDDEN, "Access denied", exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneric(Exception exception, HttpServletRequest request) {
        return buildErrorView("error/500", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", exception.getMessage(), request);
    }

    private ModelAndView buildErrorView(
            String viewName,
            HttpStatus status,
            String title,
            String message,
            HttpServletRequest request
    ) {
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.setStatus(status);
        modelAndView.addObject("title", title);
        modelAndView.addObject("message", message);
        modelAndView.addObject("path", request.getRequestURI());
        modelAndView.addObject("appName", appName);
        return modelAndView;
    }
}
