package com.viviframework.petapojo;

public class PetaPojoException extends RuntimeException {

    public PetaPojoException() {
    }

    public PetaPojoException(String message) {
        super(message);
    }

    public PetaPojoException(String message, Throwable cause) {
        super(message, cause);
    }

    public PetaPojoException(Throwable cause) {
        super(cause);
    }
}
