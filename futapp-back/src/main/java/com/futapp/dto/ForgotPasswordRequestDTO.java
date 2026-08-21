package com.futapp.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record ForgotPasswordRequestDTO(@Email @NotBlank String email) {}
