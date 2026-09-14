package com.futapp.exception;

public class TooManyLoginAttemptsException extends RuntimeException {
    public TooManyLoginAttemptsException() { super("Demasiados intentos. Esperá 15 minutos antes de volver a ingresar"); }
}
