package com.logistics.core.shipments.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.logistics.core.shipments.domain.OrderNotFoundException;
import com.logistics.core.shipments.domain.RelatedOrderNotFoundException;
import com.logistics.core.shipments.domain.ShipmentNotFoundException;

@RestControllerAdvice
public class ShipmentExceptionHandler {

    @ExceptionHandler({ShipmentNotFoundException.class, RelatedOrderNotFoundException.class, OrderNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }
}
