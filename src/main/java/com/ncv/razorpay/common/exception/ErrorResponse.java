package com.ncv.razorpay.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)//all the null value will be excluded from the response as in case when we are not passing field Errors.
public record ErrorResponse(
        String errorCode,
        String errorDescription,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors //input validations errors like wrong password etc(Validation related exception)

) {
    public record FieldError(String field,String message)//field --> name , password message --> error message
    {}

    public static ErrorResponse of(String errorCode,String errorDescription){
        return new ErrorResponse(errorCode,errorDescription,LocalDateTime.now(),null);
    }

    public static ErrorResponse of(String errorCode,String errorDescription,List<FieldError> fieldErrors){
        return new ErrorResponse(errorCode,errorDescription,LocalDateTime.now(),fieldErrors);
    }

}
