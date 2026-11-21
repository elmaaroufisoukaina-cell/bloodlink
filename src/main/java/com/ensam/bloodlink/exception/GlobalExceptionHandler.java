package com.ensam.bloodlink.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

// gestion des erreurs pour toute l'application
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String introuvable(ResourceNotFoundException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(AccesInterditException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String interdit(AccesInterditException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/403";
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String erreurMetier(BusinessException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/erreur";
    }
}