package ru.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

/** RU: Класс глобальной обработки исключений(ошибок) контроллеров */
@ControllerAdvice
public class ControllerExceptionHandler {

    /** RU: AccessDeniedException в проекте вызывается @PreAuthorize
     * в зависимости от ошибки возвращает 403 или 401 код-статус */
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Void> accessDeniedException(AccessDeniedException e) {
        HttpStatus status;
        if(Objects.equals(e.getMessage(), "is not Authenticated")) {
            status = HttpStatus.UNAUTHORIZED;
        } else {
            status = HttpStatus.FORBIDDEN;
        }
        return ResponseEntity.status(status).build();
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Void> globalExceptionHandler() {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
}
