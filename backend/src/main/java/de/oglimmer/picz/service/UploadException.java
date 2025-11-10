package de.oglimmer.picz.service;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class UploadException extends RuntimeException {

    @Getter
    private HttpStatus httpStatus;

    public UploadException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

}
